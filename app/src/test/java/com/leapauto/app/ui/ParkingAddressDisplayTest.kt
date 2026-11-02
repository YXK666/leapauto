package com.leapauto.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParkingAddressDisplayTest {

    @Test
    fun `short address becomes primary line and full address stays as secondary`() {
        val display = parkingAddressDisplay(
            shortAddress = "蔚蓝海岸 3 号楼",
            fullAddress = "广东省深圳市南山区海德三道蔚蓝海岸3号楼"
        )

        assertEquals("蔚蓝海岸 3 号楼", display?.primary)
        assertEquals("广东省深圳市南山区海德三道蔚蓝海岸3号楼", display?.secondary)
        assertEquals("广东省深圳市南山区海德三道蔚蓝海岸3号楼", display?.clipboardText)
    }

    @Test
    fun `full address only is shown as primary without duplicate secondary line`() {
        val display = parkingAddressDisplay(
            shortAddress = "",
            fullAddress = "浙江省杭州市余杭区文一西路 969 号"
        )

        assertEquals("浙江省杭州市余杭区文一西路 969 号", display?.primary)
        assertNull(display?.secondary)
        assertEquals("浙江省杭州市余杭区文一西路 969 号", display?.clipboardText)
    }

    @Test
    fun `short address only is shown and copied when full address is missing`() {
        val display = parkingAddressDisplay(
            shortAddress = "零跑汽车杭州旗舰店",
            fullAddress = null
        )

        assertEquals("零跑汽车杭州旗舰店", display?.primary)
        assertNull(display?.secondary)
        assertEquals("零跑汽车杭州旗舰店", display?.clipboardText)
    }

    @Test
    fun `identical short and full address are not duplicated`() {
        val display = parkingAddressDisplay(
            shortAddress = "未来科技城",
            fullAddress = "未来科技城"
        )

        assertEquals("未来科技城", display?.primary)
        assertNull(display?.secondary)
    }

    @Test
    fun `blank values return null so the dialog can show the loading placeholder`() {
        assertNull(parkingAddressDisplay(shortAddress = null, fullAddress = null))
        assertNull(parkingAddressDisplay(shortAddress = "", fullAddress = "   "))
    }

    @Test
    fun `surrounding whitespace is trimmed before display and copy`() {
        val display = parkingAddressDisplay(
            shortAddress = "  龙湖天街  ",
            fullAddress = "  上海市闵行区龙湖天街  "
        )

        assertEquals("龙湖天街", display?.primary)
        assertEquals("上海市闵行区龙湖天街", display?.secondary)
        assertEquals("上海市闵行区龙湖天街", display?.clipboardText)
    }
}
