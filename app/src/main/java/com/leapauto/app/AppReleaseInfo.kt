package com.leapauto.app

object AppReleaseInfo {
    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    // Update this text together with apkVersionName before each delivery package.
    const val currentReleaseNotes = "1. 驻车实景弹窗展示车辆位置中文地址，支持一键复制地址\n2. 新增车辆位置小地图，点击可直接跳转外部地图\n3. 位置解析失败时显示具体原因，支持点击重新获取地址\n4. 高德 Web 服务配置页可查看最近一次请求诊断信息"
}
