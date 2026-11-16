package com.leapauto.app

object AppReleaseInfo {
    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    // Update this text together with apkVersionName before each delivery package.
    const val currentReleaseNotes = "1. 设置界面恢复【版本信息与检查更新】卡片\n2. 优化开源代码卡片文字展示\n3. 新增定时空调：快捷面板与 4x2 桌面插件可设置到点自动开空调\n4. 修复定时空调到时间之后没有联动关闭车窗的问题\n5. 优化定时空调时间选择跟手性，并增加震动反馈\n6. 优化桌面组件高度和车图扁平比（若不生效请删除组件重新添加）\n7. 驻车实景弹窗展示车辆位置中文地址与小地图，支持一键复制、失败重试\n8. 无实景照片时自动隐藏照片区域，适配无环视相机的车型"
}
