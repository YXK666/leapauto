package com.leapauto.app

import android.content.Context
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GeocodedAddress(
    val shortAddress: String,
    val fullAddress: String,
    val adcode: String = "",
    val city: String = ""
)

/**
 * 最近一次车辆位置逆地理编码的诊断结果，用于车主自查（我的 → 高德 Web 服务配置）。
 * 只记录请求结果状态，不记录车辆经纬度。
 */
data class RegeoDiagnostic(
    val outcome: String,
    val requestUrl: String,
    val httpCode: Int? = null,
    val status: String? = null,
    val infocode: String? = null,
    val info: String? = null,
    val shortAddress: String? = null,
    val fullAddress: String? = null,
    val durationMs: Long = 0L,
    val epochMs: Long = System.currentTimeMillis(),
    val fromCache: Boolean = false
) {
    /** 面向车主的单行中文描述。 */
    fun describe(): String = when (outcome) {
        OUTCOME_SUCCESS -> if (fromCache) {
            "命中缓存：${shortAddress.orEmpty()}"
        } else {
            "成功：${shortAddress.orEmpty()}（${durationMs}ms）"
        }

        OUTCOME_NO_KEY -> "未配置高德 Web 服务 Key，未发起请求"
        OUTCOME_HTTP_ERROR -> "HTTP ${httpCode ?: "-"} ${info.orEmpty()}".trim()
        OUTCOME_API_ERROR -> "高德拒绝：status=${status.orEmpty()} infocode=${infocode.orEmpty()} ${info.orEmpty()}".trim()
        OUTCOME_NETWORK_ERROR -> "网络失败：${info.orEmpty()}".trim()
        OUTCOME_PARSE_ERROR -> "响应解析失败：${info.orEmpty()}".trim()
        else -> outcome
    }

    companion object {
        const val OUTCOME_SUCCESS = "SUCCESS"
        const val OUTCOME_NO_KEY = "NO_KEY"
        const val OUTCOME_HTTP_ERROR = "HTTP_ERROR"
        const val OUTCOME_API_ERROR = "API_ERROR"
        const val OUTCOME_NETWORK_ERROR = "NETWORK_ERROR"
        const val OUTCOME_PARSE_ERROR = "PARSE_ERROR"
    }
}

object VehicleLocationGeocoder {
    private const val REGEO_URL = "https://restapi.amap.com/v3/geocode/regeo"
    private const val MAX_CACHE_ENTRIES = 20
    private const val CACHE_DISTANCE_THRESHOLD_METERS = 80.0 // 80米范围内直接命中缓存
    private const val CACHE_EXPIRY_MS = 2 * 60 * 60 * 1000L // 2小时有效期
    private const val TIMEOUT_MS = 3_500L

    private data class CachedGeo(
        val latitude: Double,
        val longitude: Double,
        val address: GeocodedAddress,
        val cachedAtEpochMs: Long
    )

    private val cache = mutableListOf<CachedGeo>()

    /** 最近一次请求的结果诊断，供 UI 展示与排查。 */
    @Volatile
    var lastDiagnostic: RegeoDiagnostic? = null
        private set

    @Synchronized
    fun clearCache() {
        cache.clear()
    }

    @Synchronized
    fun clearDiagnostic() {
        lastDiagnostic = null
    }

    fun reverseGeocode(
        latitude: Double,
        longitude: Double,
        apiKey: String = ObfuscatedSecrets.getAmapWebKey(),
        context: Context? = null
    ): GeocodedAddress? {
        val startedAt = System.currentTimeMillis()
        val now = System.currentTimeMillis()

        // 1. 优先检查高命中率内存距离缓存 (0ms 瞬间响应)
        synchronized(this) {
            val cached = cache.firstOrNull { entry ->
                now - entry.cachedAtEpochMs < CACHE_EXPIRY_MS &&
                    calculateDistanceMeters(latitude, longitude, entry.latitude, entry.longitude) <= CACHE_DISTANCE_THRESHOLD_METERS
            }
            if (cached != null) {
                lastDiagnostic = RegeoDiagnostic(
                    outcome = RegeoDiagnostic.OUTCOME_SUCCESS,
                    requestUrl = "内存缓存命中，未发起网络请求",
                    shortAddress = cached.address.shortAddress,
                    fullAddress = cached.address.fullAddress,
                    fromCache = true
                )
                return cached.address
            }
        }

        val candidates = if (apiKey.isNotBlank() && apiKey != ObfuscatedSecrets.getAmapWebKey()) {
            listOf(apiKey)
        } else {
            AmapApiKeyManager.getCandidateKeys(context)
        }

        if (candidates.isEmpty()) {
            lastDiagnostic = RegeoDiagnostic(
                outcome = RegeoDiagnostic.OUTCOME_NO_KEY,
                requestUrl = "未配置专属 Key"
            )
            return null
        }
        val location = String.format(Locale.US, "%.6f,%.6f", longitude, latitude)

        for (key in candidates) {
            val urlString = "$REGEO_URL?key=$key&location=$location&extensions=all"
            var parsed: GeocodedAddress? = null

            try {
                val client = NetworkDebugController.httpClient()
                    .newBuilder()
                    .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .build()
                val request = Request.Builder()
                    .url(urlString)
                    .get()
                    .header("User-Agent", "LeapAuto/${BuildConfig.VERSION_NAME}")
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    val elapsed = System.currentTimeMillis() - startedAt

                    if (!response.isSuccessful) {
                        lastDiagnostic = RegeoDiagnostic(
                            outcome = RegeoDiagnostic.OUTCOME_HTTP_ERROR,
                            requestUrl = maskKey(urlString),
                            httpCode = response.code,
                            info = "接口返回 HTTP ${response.code}",
                            durationMs = elapsed
                        )
                        return@use
                    }

                    val json = JSONObject(body)
                    val status = json.optString("status")
                    val infocode = json.optString("infocode")
                    val info = json.optString("info")

                    if (status != "1") {
                        val quotaExhausted = AmapApiKeyManager.isQuotaExhausted(status, infocode)
                        if (quotaExhausted) {
                            AmapApiKeyManager.markQuotaExhausted(key, info)
                        }
                        lastDiagnostic = RegeoDiagnostic(
                            outcome = RegeoDiagnostic.OUTCOME_API_ERROR,
                            requestUrl = maskKey(urlString),
                            httpCode = response.code,
                            status = status,
                            infocode = infocode,
                            info = if (quotaExhausted) "$info（该 Key 已冷冻 4 小时）" else info,
                            durationMs = elapsed
                        )
                        return@use
                    }

                    val regeocode = json.optJSONObject("regeocode")
                    if (regeocode == null) {
                        lastDiagnostic = RegeoDiagnostic(
                            outcome = RegeoDiagnostic.OUTCOME_PARSE_ERROR,
                            requestUrl = maskKey(urlString),
                            httpCode = response.code,
                            status = status,
                            infocode = infocode,
                            info = "响应缺少 regeocode 字段",
                            durationMs = elapsed
                        )
                        return@use
                    }

                    val addressComponent = regeocode.optJSONObject("addressComponent") ?: JSONObject()
                    val adcode = addressComponent.optString("adcode").trim()
                    val cityRaw = addressComponent.optString("city").trim().takeUnless { it == "[]" || it.isBlank() }
                        ?: addressComponent.optString("province").trim()
                    val shortAddr = formatShortAddress(regeocode)
                    val fullAddr = regeocode.optString("formatted_address").trim().ifBlank { shortAddr }
                    parsed = GeocodedAddress(
                        shortAddress = shortAddr,
                        fullAddress = fullAddr,
                        adcode = adcode,
                        city = cityRaw
                    )
                    lastDiagnostic = RegeoDiagnostic(
                        outcome = RegeoDiagnostic.OUTCOME_SUCCESS,
                        requestUrl = maskKey(urlString),
                        httpCode = response.code,
                        status = status,
                        infocode = infocode,
                        shortAddress = shortAddr,
                        fullAddress = fullAddr,
                        durationMs = elapsed
                    )
                }
            } catch (e: Exception) {
                lastDiagnostic = RegeoDiagnostic(
                    outcome = RegeoDiagnostic.OUTCOME_NETWORK_ERROR,
                    requestUrl = maskKey(urlString),
                    info = "${e.javaClass.simpleName}: ${e.message?.take(80).orEmpty()}",
                    durationMs = System.currentTimeMillis() - startedAt
                )
            }

            if (parsed != null) {
                // 存入内存缓存
                synchronized(this) {
                    if (cache.size >= MAX_CACHE_ENTRIES) {
                        cache.removeAt(0)
                    }
                    cache.add(CachedGeo(latitude, longitude, parsed!!, now))
                }
                return parsed
            }
        }
        return null
    }

    /** 展示用的请求地址中，Key 只保留前 4 位与后 4 位。 */
    private fun maskKey(url: String): String {
        val marker = "key="
        val start = url.indexOf(marker)
        if (start < 0) return url
        val valueStart = start + marker.length
        var end = url.indexOf('&', valueStart)
        if (end < 0) end = url.length
        val raw = url.substring(valueStart, end)
        val masked = if (raw.length > 8) "${raw.take(4)}****${raw.takeLast(4)}" else "****"
        return url.substring(0, valueStart) + masked + url.substring(end)
    }

    internal fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371000.0 * c
    }

    internal fun formatShortAddress(regeocode: JSONObject): String {
        val addressComponent = regeocode.optJSONObject("addressComponent") ?: JSONObject()
        val province = addressComponent.optString("province").trim()
        val cityRaw = addressComponent.optString("city").trim().takeUnless { it == "[]" || it.isBlank() }
            ?: province
        val city = cityRaw.removeSuffix("市").removeSuffix("地区").removeSuffix("特别行政区").trim()

        val formattedAddress = regeocode.optString("formatted_address").trim()
        val district = addressComponent.optString("district").trim().takeUnless { it == "[]" }.orEmpty()
        val township = addressComponent.optString("township").trim().takeUnless { it == "[]" }.orEmpty()

        // 1. Try AOI (Area of Interest, e.g. residential compound, mall, building complex)
        val aois = regeocode.optJSONArray("aois")
        val aoiName = (0 until (aois?.length() ?: 0))
            .mapNotNull { aois?.optJSONObject(it)?.optString("name")?.trim() }
            .firstOrNull { it.isNotBlank() }

        // 2. Try stripping administrative prefixes (province, city, district, township)
        var remainder = formattedAddress
        if (province.isNotBlank()) remainder = remainder.removePrefix(province)
        val cityToRemove = addressComponent.optString("city").trim().takeUnless { it == "[]" }.orEmpty()
        if (cityToRemove.isNotBlank()) remainder = remainder.removePrefix(cityToRemove)
        if (district.isNotBlank()) remainder = remainder.removePrefix(district)
        if (township.isNotBlank()) remainder = remainder.removePrefix(township)
        remainder = remainder.trim()

        // 3. Fallback to POI if remainder is blank
        val poiName = if (aoiName.isNullOrBlank() && remainder.isBlank()) {
            val pois = regeocode.optJSONArray("pois")
            (0 until (pois?.length() ?: 0))
                .mapNotNull { pois?.optJSONObject(it)?.optString("name")?.trim() }
                .firstOrNull { it.isNotBlank() }
        } else null

        val specific = (aoiName ?: remainder.takeIf { it.isNotBlank() } ?: poiName ?: formattedAddress).trim()

        return specific
            .removePrefix(cityRaw).trim()
            .removePrefix(city).trim()
            .ifBlank { specific }
    }
}
