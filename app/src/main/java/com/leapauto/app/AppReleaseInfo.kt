package com.leapauto.app

object AppReleaseInfo {
    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    // Update this text together with apkVersionName before each delivery package.
    const val currentReleaseNotes = "1. 修复桌面插件点击响应（Android 12+ 后台启动限制导致按钮失灵）\n2. 修复桌面插件空白处点击无响应，现可点击插件任意位置打开 App\n3. 修复余晖石筑主题浅底文字对比度不足，改用深色文字\n4. 驻车实景弹窗在无实景照片时自动隐藏照片区域，不再显示占位提示\n5. 桌面小组件支持显示爱车真实昵称，不再固定显示车型名\n6. 驻车实景弹窗展示车辆位置中文地址，支持一键复制地址\n7. 新增车辆位置小地图，点击可直接跳转外部地图\n8. 位置解析失败时显示具体原因，支持点击重新获取地址\n9. 高德 Web 服务配置页可查看最近一次请求诊断信息\n10. 校准 3D 车模三维投影靶心与镜头视角，展台区域黄金对称居中"
}
