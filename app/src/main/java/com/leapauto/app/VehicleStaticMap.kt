package com.leapauto.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 车辆位置静态小地图（高德静态地图 API，图片仅保存在内存中，不落盘）。
 */
object VehicleStaticMap {
    private const val STATIC_MAP_URL = "https://restapi.amap.com/v3/staticmap"
    private const val ZOOM = 16
    private const val WIDTH = 600
    private const val HEIGHT = 360
    private const val SCALE = 2
    private const val MARKER_COLOR = "0xFF3B30" // 高德要求 0xRRGGBB 六位格式
    // 图钉上的单个中文字标签“车”，已做百分号编码
    private const val MARKER_LABEL = "%E8%BD%A6"
    private const val CONNECT_TIMEOUT_MS = 5_000L
    private const val READ_TIMEOUT_MS = 8_000L
    private const val CACHE_EXPIRY_MS = 2 * 60 * 60 * 1000L
    private const val CACHE_DISTANCE_THRESHOLD_METERS = 80.0
    private const val MAX_CACHE_ENTRIES = 6

    private data class CachedMap(
        val latitude: Double,
        val longitude: Double,
        val bitmap: Bitmap,
        val cachedAtEpochMs: Long
    )

    private val cache = mutableListOf<CachedMap>()

    /** 最近一次失败原因，供 UI 与排查使用（不记录经纬度）。 */
    @Volatile
    var lastFailure: String? = null
        private set

    @Synchronized
    fun clearCache() {
        cache.clear()
    }

    fun buildUrl(latitude: Double, longitude: Double, apiKey: String): String {
        val location = String.format(Locale.US, "%.6f,%.6f", longitude, latitude)
        return "$STATIC_MAP_URL?location=$location&zoom=$ZOOM" +
            "&size=${WIDTH}*${HEIGHT}&scale=$SCALE" +
            "&markers=mid,$MARKER_COLOR,$MARKER_LABEL:$location" +
            "&key=$apiKey"
    }

    fun fetch(context: Context, latitude: Double, longitude: Double): Bitmap? {
        val now = System.currentTimeMillis()

        synchronized(this) {
            val hit = cache.firstOrNull { entry ->
                now - entry.cachedAtEpochMs < CACHE_EXPIRY_MS &&
                    VehicleLocationGeocoder.calculateDistanceMeters(
                        latitude, longitude, entry.latitude, entry.longitude
                    ) <= CACHE_DISTANCE_THRESHOLD_METERS
            }
            if (hit != null) {
                lastFailure = null
                return hit.bitmap
            }
        }

        val apiKey = AmapApiKeyManager.getCandidateKeys(context).firstOrNull()
        if (apiKey == null) {
            lastFailure = "未配置高德 Web 服务 Key"
            return null
        }

        val url = buildUrl(latitude, longitude, apiKey)
        try {
            val client = NetworkDebugController.httpClient()
                .newBuilder()
                .connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build()
            val request = Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", "LeapAuto/${BuildConfig.VERSION_NAME}")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    lastFailure = "图片接口返回 HTTP ${response.code}"
                    return null
                }
                val contentType = response.body?.contentType()?.toString().orEmpty()
                val bytes = response.body?.bytes()
                if (bytes == null || bytes.isEmpty()) {
                    lastFailure = "图片接口返回内容为空"
                    return null
                }
                if (contentType.contains("json", ignoreCase = true)) {
                    val json = runCatching { JSONObject(String(bytes, Charsets.UTF_8)) }.getOrNull()
                    lastFailure = "静态地图被拒绝：${json?.optString("infocode").orEmpty()} ${json?.optString("info").orEmpty()}".trim()
                    return null
                }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap == null) {
                    lastFailure = "图片解码失败"
                    return null
                }
                synchronized(this) {
                    if (cache.size >= MAX_CACHE_ENTRIES) {
                        cache.removeAt(0)
                    }
                    cache.add(CachedMap(latitude, longitude, bitmap, now))
                }
                lastFailure = null
                return bitmap
            }
        } catch (e: Exception) {
            lastFailure = "${e.javaClass.simpleName}: ${e.message?.take(80).orEmpty()}".trim()
            return null
        }
    }
}
