package com.leapauto.app

object AppReleaseInfo {
    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    // Update this text together with apkVersionName before each delivery package.
    const val currentReleaseNotes = "1. 驻车实景弹窗在无实景照片时自动隐藏照片区域，不再显示占位提示\n2. 桌面小组件支持显示爱车真实昵称，不再固定显示车型名\n3. 驻车实景弹窗展示车辆位置中文地址，支持一键复制地址\n4. 新增车辆位置小地图，点击可直接跳转外部地图\n5. 位置解析失败时显示具体原因，支持点击重新获取地址\n6. 高德 Web 服务配置页可查看最近一次请求诊断信息\n7. 校准 3D 车模三维投影靶心与镜头视角，展台区域黄金对称居中\n8. 首页充电胶囊精简优化，待机与解锁后统一显示微晶“充电中心”\n9. 4x2 桌面插件全面重构为差量无感静默刷新，彻底杜绝日常闪烁"
}
