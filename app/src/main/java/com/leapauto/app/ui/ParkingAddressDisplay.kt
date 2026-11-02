package com.leapauto.app.ui

/**
 * 驻车实景弹窗内的车辆位置中文描述展示模型。
 *
 * - [primary]：优先展示高德逆地理编码解析出的短地址（小区/园区/POI 名称），缺失时退回完整地址；
 * - [secondary]：完整地址，仅在与主行不同且非空时展示，便于车主确认具体街道门牌；
 * - [clipboardText]：用户主动点击复制时写入剪贴板的文本，优先完整地址。
 */
internal data class ParkingAddressDisplay(
    val primary: String,
    val secondary: String? = null
) {
    val clipboardText: String
        get() = secondary ?: primary
}

/**
 * 组合短地址与完整地址；两者都为空时返回 null，由界面展示占位文案。
 */
internal fun parkingAddressDisplay(
    shortAddress: String?,
    fullAddress: String?
): ParkingAddressDisplay? {
    val short = shortAddress?.trim().orEmpty()
    val full = fullAddress?.trim().orEmpty()
    if (short.isBlank() && full.isBlank()) return null

    val primary = short.ifBlank { full }
    val secondary = full.takeIf { it.isNotBlank() && it != primary }
    return ParkingAddressDisplay(primary = primary, secondary = secondary)
}
