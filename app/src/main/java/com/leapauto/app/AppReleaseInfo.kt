package com.leapauto.app

object AppReleaseInfo {
    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    // Update this text together with apkVersionName before each delivery package.
    const val currentReleaseNotes = "1. 定时空调支持联动开启座椅通风、座椅加热和方向盘加热\n2. 定时空调面板重构：设置定时任务时可勾选座舱舒适联动项\n3. 4x2 桌面插件新增中性状态图标，观感更细腻\n4. 设置界面恢复【版本信息与检查更新】卡片\n5. 驻车实景弹窗展示车辆位置中文地址与小地图，支持一键复制、失败重试\n6. 无实景照片时自动隐藏照片区域，适配无环视相机的车型\n7. 定时空调到点自动开空调并联动关闭车窗"
}
