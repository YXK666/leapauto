package com.leapauto.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegeoDiagnosticTest {

    @Test
    fun `success diagnostic shows short address and duration`() {
        val diagnostic = RegeoDiagnostic(
            outcome = RegeoDiagnostic.OUTCOME_SUCCESS,
            requestUrl = "https://restapi.amap.com/v3/geocode/regeo?key=abcd****wxyz",
            shortAddress = "蔚蓝海岸 3 号楼",
            durationMs = 320
        )

        assertEquals("成功：蔚蓝海岸 3 号楼（320ms）", diagnostic.describe())
    }

    @Test
    fun `cache hit diagnostic explains no network request was made`() {
        val diagnostic = RegeoDiagnostic(
            outcome = RegeoDiagnostic.OUTCOME_SUCCESS,
            requestUrl = "内存缓存命中，未发起网络请求",
            shortAddress = "未来科技城",
            fromCache = true
        )

        assertEquals("命中缓存：未来科技城", diagnostic.describe())
    }

    @Test
    fun `missing key diagnostic tells the owner to configure a key`() {
        val diagnostic = RegeoDiagnostic(
            outcome = RegeoDiagnostic.OUTCOME_NO_KEY,
            requestUrl = "未配置专属 Key"
        )

        assertEquals("未配置高德 Web 服务 Key，未发起请求", diagnostic.describe())
    }

    @Test
    fun `api error diagnostic includes status infocode and message`() {
        val diagnostic = RegeoDiagnostic(
            outcome = RegeoDiagnostic.OUTCOME_API_ERROR,
            requestUrl = "https://restapi.amap.com/v3/geocode/regeo?key=abcd****wxyz",
            httpCode = 200,
            status = "0",
            infocode = "10009",
            info = "请求来源未被授权"
        )

        assertEquals("高德拒绝：status=0 infocode=10009 请求来源未被授权", diagnostic.describe())
    }

    @Test
    fun `http error diagnostic includes the status code`() {
        val diagnostic = RegeoDiagnostic(
            outcome = RegeoDiagnostic.OUTCOME_HTTP_ERROR,
            requestUrl = "https://restapi.amap.com/v3/geocode/regeo?key=abcd****wxyz",
            httpCode = 502,
            info = "接口返回 HTTP 502"
        )

        assertEquals("HTTP 502 接口返回 HTTP 502", diagnostic.describe())
    }

    @Test
    fun `network error diagnostic includes the failure detail`() {
        val diagnostic = RegeoDiagnostic(
            outcome = RegeoDiagnostic.OUTCOME_NETWORK_ERROR,
            requestUrl = "https://restapi.amap.com/v3/geocode/regeo?key=abcd****wxyz",
            info = "SocketTimeoutException: timeout"
        )

        assertEquals("网络失败：SocketTimeoutException: timeout", diagnostic.describe())
    }

    @Test
    fun `diagnostic carries response fields for the settings page`() {
        val diagnostic = RegeoDiagnostic(
            outcome = RegeoDiagnostic.OUTCOME_API_ERROR,
            requestUrl = "https://restapi.amap.com/v3/geocode/regeo?key=abcd****wxyz",
            httpCode = 200,
            status = "0",
            infocode = "10009"
        )

        assertEquals(200, diagnostic.httpCode)
        assertEquals("0", diagnostic.status)
        assertEquals("10009", diagnostic.infocode)
        assertEquals(0L, diagnostic.durationMs)
        assertTrue(diagnostic.epochMs > 0L)
    }
}
