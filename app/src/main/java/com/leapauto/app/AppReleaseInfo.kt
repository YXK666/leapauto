package com.leapauto.app

object AppReleaseInfo {
    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    // Update this text together with apkVersionName before each delivery package.
    const val currentReleaseNotes = "1. 驻车实景弹窗展示车辆位置中文地址，支持一键复制地址\n2. 新增车辆位置小地图，点击可直接跳转外部地图\n3. 位置解析失败时显示具体原因，支持点击重新获取地址\n4. 高德 Web 服务配置页可查看最近一次请求诊断信息\n5. 首页充电胶囊精简优化，待机与解锁后统一显示微晶“充电中心”\n6. 4x2 桌面插件全面重构为差量无感静默刷新，彻底杜绝日常闪烁\n7. 优化 4x2 插件全透微晶模式下 km 单位与更新时间为高对比度黑色\n8. 优化 4x2 插件续航进度条底槽与微晶切边对比度，清晰展现已消耗续航\n9. 优化 4x2 插件“行驶中...”为科技极速青高辨识度显示，设置页卡片增加主题微晕"
}
