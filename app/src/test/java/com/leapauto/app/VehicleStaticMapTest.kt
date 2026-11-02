package com.leapauto.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleStaticMapTest {

    private val testKey = "0123456789abcdef0123456789abcdef"

    @Test
    fun `static map url uses longitude latitude order required by amap`() {
        val url = VehicleStaticMap.buildUrl(30.123456, 114.987654, testKey)

        assertTrue(url.contains("location=114.987654,30.123456"))
        assertTrue(url.contains("zoom=16"))
        assertTrue(url.contains("size=600*360"))
        assertTrue(url.contains("scale=2"))
    }

    @Test
    fun `static map url marks the vehicle position at the same coordinate`() {
        val url = VehicleStaticMap.buildUrl(30.123456, 114.987654, testKey)

        assertTrue(url.contains("markers=mid,0xFF3B30,%E8%BD%A6:114.987654,30.123456"))
        assertTrue(url.contains("key=$testKey"))
    }

    @Test
    fun `static map url keeps six decimal places for coordinates`() {
        val url = VehicleStaticMap.buildUrl(30.1, 114.9, testKey)

        assertTrue(url.contains("location=114.900000,30.100000"))
    }

    @Test
    fun `coordinate rounding matches the shared distance helper`() {
        // 80 米内命中缓存，跨越城市则远超阈值
        val near = VehicleLocationGeocoder.calculateDistanceMeters(30.12, 114.98, 30.1205, 114.9805)
        val far = VehicleLocationGeocoder.calculateDistanceMeters(30.12, 114.98, 31.12, 115.98)

        assertTrue(near < 80.0)
        assertTrue(far > 80.0)
        assertEquals(0.0, VehicleLocationGeocoder.calculateDistanceMeters(30.12, 114.98, 30.12, 114.98), 0.001)
    }
}
