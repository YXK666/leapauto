package com.leapauto.app

object AppReleaseInfo {
    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    // Update this text together with apkVersionName before each delivery package.
    const val currentReleaseNotes = "1. 新增充电记录与行驶记录，可查看历史充电明细与油电行程\n2. 直进直出遥控挪车：按住持续行驶、松手即刹，前后台生命周期自动熔断急停\n3. 直进直出快捷按键全新高辨识度图标，操控盘视觉精简\n4. 蓝牙钥匙连接稳定性加固：GATT 假死瞬态自愈、自动跳过异常 MTU 快速直连\n5. 增程车型油电行驶记录\n6. 驻车实景弹窗展示车辆位置中文地址与小地图，支持一键复制、失败重试\n7. 无实景照片时自动隐藏照片区域，适配无环视相机的车型\n8. 定时空调支持联动座椅通风/加热、方向盘加热并自动关窗"
}
