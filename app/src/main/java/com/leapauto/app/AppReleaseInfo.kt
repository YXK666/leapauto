package com.leapauto.app

object AppReleaseInfo {
    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    // Update this text together with apkVersionName before each delivery package.
    const val currentReleaseNotes = "1. 空调与定时空调界面 UI 全面重构，新增机械触感震动反馈\n2. 充电记录增加月度与年度充电金额展示\n3. 桌面小组件优化交互状态色彩、控车转圈动效\n4. 新增车内温度与充电功率展示、前备箱专属矢量图标\n5. 新增充电记录与行驶记录，可查看历史充电明细与油电行程\n6. 驻车实景弹窗展示车辆位置中文地址与小地图，支持一键复制、失败重试\n7. 无实景照片时自动隐藏照片区域，适配无环视相机的车型\n8. 定时空调支持联动座椅通风/加热、方向盘加热并自动关窗"
}
