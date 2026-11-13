package com.leapauto.app

object AppReleaseInfo {
    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    // Update this text together with apkVersionName before each delivery package.
    const val currentReleaseNotes = "1. 新增定时空调：快捷面板与 4x2 桌面插件可设置到点自动开空调\n2. 修复定时空调到时间之后没有联动关闭车窗的问题\n3. 优化定时空调时间选择跟手性，并增加震动反馈\n4. 优化桌面组件高度和车图扁平比（若不生效请删除组件重新添加）\n5. 主页底部能量卡片高度自适应\n6. 驻车实景弹窗展示车辆位置中文地址与小地图，支持一键复制、失败重试\n7. 无实景照片时自动隐藏照片区域，适配无环视相机的车型\n8. 高德 Web 服务配置页可查看最近一次请求诊断信息"
}
