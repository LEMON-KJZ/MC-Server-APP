# Minecraft Server Control Android

这是 Minecraft Server API 的 Android 客户端，使用 Jetpack Compose Material 3，并集成 Kyant0/AndroidLiquidGlass 的 `LiquidBottomTabs` / `LiquidBottomTab` Dock 示例。

## 三页结构

- **首页**：连接状态、在线人数、TPS/MSPT、内存、运行时间、在线玩家、最近日志。
- **服务器杂项**：控制台指令、公告、踢出、封禁、解封、白名单、OP、撤销 OP、私聊、游戏规则。
- **设置**：API 地址/端口、API Key、HTTPS、自动刷新、通知、浅色/深色模式，以及“关于作者”信息。

默认使用浅色界面；在“设置 → 客户端设置”中打开“深色模式”即可切换。页面会根据可用宽度限制内容最大宽度，并为底部手势导航条、键盘和刘海/状态栏保留安全区。

## 构建

需要 Android Studio、Android SDK 37、JDK 17+ 和网络访问 Maven Central/Google Maven：

```text
gradlew.bat :app:assembleDebug
```

APK 输出：`app/build/outputs/apk/debug/app-debug.apk`。

本项目已在 Windows 环境使用 Gradle 9.8.0 构建验证，输出的是带调试签名、可直接安装的 APK。正式发布到应用商店前，请使用自己的发布签名构建 release APK。

## 服务器配置

在设置页填写：

```text
API 地址：http://你的域名:8080/
API Key：服务器首次启动时生成的 Key
```

如果服务端开启 HTTPS，勾选 HTTPS，并使用 `https://` 地址。

## 第三方许可

`AndroidLiquidGlass` 的 Backdrop 库和 Dock 示例使用 Apache-2.0 许可，见 `ANDROID_LIQUID_GLASS_LICENSE` 与 `THIRD_PARTY_NOTICES.md`。
