# 校园传呼 · Campus Paging

**校园呼叫播报工具** —— 手机发送端一键呼叫，教室大屏（电脑）接收端实时大字显示 + 语音播报。

- 版本：**1.4.0**
- 开发团队：**symonolith**
- 消息经 MQTT 公共服务器（`wss://broker-cn.emqx.io:8084/mqtt`）转发，主题 `campus/call/房间号`，无需注册、无需自建服务器。

## 下载

| 平台 | 文件 | 说明 |
|---|---|---|
| Android 手机 | `CampusPager-v1.4.0.apk` | 直接安装（需允许安装未知来源应用） |
| Windows 电脑 | `CampusPager-v1.4.0-win64.exe` | 绿色免安装，双击即用（约 155MB） |
| iPhone / iPad（iOS） | 源码工程 `CampusPager-iOS/` | 同款同功能；需在 Mac + Xcode 编译安装（详见 `CampusPager-iOS/README-iOS.md`） |

GitHub Release：https://github.com/syemailcom2026-cell/Campus-Paging/releases （历史版本均保留）

## 功能特性

- **两端互联**：填写相同 6 位数字房间号即可互呼；房间号支持「🎲 随机生成」。
- **发送端（手机）**：按班级分组管理学生、搜索、多选一次呼叫多人；常用目的地一键选择。
- **接收端（教室大屏 / 电脑）**：收到呼叫后大字显示、自动语音播报；支持播报队列与终端音量/音色设置。
- **播报参数下发**：重复遍数、重复间隔、语速、音调、音量、播报字号与文字颜色，随消息下发给大屏端。
- **名单分组管理**：班级作为分组，点进分组直接管理学生；「全部」后可「＋ 添加班级」，分组内有「＋ 添加学生」。
- **批量导入名单**：直接在输入框批量输入，支持中文逗号（，）与英文逗号（,）自动拆分——输入 `东雪莲，孙笑川，张雪松` 自动拆成 3 名学生；支持 `班级,姓名` 与 `班级,姓名1,姓名2` 格式；多行可直接换行。
- **播报模板（自定义页）**：模板编辑框上方提供**变量模块**（班级 / 姓名 / 目的地），手机端点按插入、桌面端拖拽插入；常用模板一键选用，可随时恢复默认。
- **目的地智能联动**：当播报模板不含「呼叫目的地」变量时，呼叫页自动隐藏「呼叫目的地」板块。
- **设置页功能分组**：呼叫设置（参数 / 大屏样式）、个性化设置（深色模式 / UI 配色 / 应用内字体大小 / 背景图片）、连接设置、关于。
- **个性化外观**：深色 / 浅色模式、8 套 UI 配色方案（深空蓝 / 青碧 / 云霞紫 / 暖阳金 / 樱花粉 / 薄荷绿 / 朱砂红 / 曜石灰）、自定义背景图片。
- **刷新**：呼叫页支持**下拉刷新**与顶栏「↻」刷新按键，一键重载名单 / 目的地 / 记录并重连。
- **弹窗方式（1.3.1）**：接收端收到广播时在**屏幕右下角**弹出通知小窗 + 提示声，不遮挡主屏幕；通知一直挂到呼叫结束，也可手动 ✕ 关闭。
- **后台运行**：点「×」不退出，最小化到系统托盘继续运行；完全退出用托盘右键菜单。

## 使用说明

### 一、发送端（手机 / Windows 呼叫端）

顶部显示：发送端 · 房间号、接收端在线状态、实时时钟、刷新、退出。

四个标签页：

1. **呼叫**
   - 选择学生：支持搜索姓名/班级；顶部「全部」与各班分组，「＋ 添加班级」建立分组，分组内「＋ 添加学生」（支持逗号分隔一次添加多人）；
   - 点击学生选中，显示在「呼叫详情」；「多选」模式可一次呼叫多人；
   - 呼叫目的地：常用目的地一键选择，也可「更多目的地」展开或直接输入（模板不含目的地变量时自动隐藏）；
   - 附加备注（可选）；播报内容预览实时更新；发送呼叫 / 本机试听。
2. **自定义**：播报模板（变量模块点击/拖动插入、常用模板、恢复默认）；自定义目的地。
3. **记录**：查看全部呼叫收发记录，可一键清空。
4. **设置**：按「呼叫设置 / 个性化设置 / 连接设置 / 关于」分组展示。

### 二、接收端（教室大屏 / Windows 电脑）

1. 双击运行，输入 6 位房间号，选择「接收端」；
2. Windows 出现蓝色「已保护你的电脑」提示 → 更多信息 → 仍要运行；
3. 缺 WebView2 时到微软官网安装：https://developer.microsoft.com/microsoft-edge/webview2/

接收端功能：
- 收到呼叫后**右下角弹出通知**（提示声 + 挂到呼叫结束可关闭），大屏主界面同步大字显示播报内容；
- 播报队列：待播报内容依次排队，可清空；
- 终端设置：主音量、播报音色、测试播报、停止。

### 三、多端互联

- 手机发送 → 大屏播报（参数随消息下发）；
- 发送端顶部可看到「接收端在线」状态；
- 只要两端 6 位房间号相同即可，任意组合可互呼。

注意：公共 Broker 消息公开，勿传隐私信息；可在「连接设置」换成自己的服务器。

## 常见问题

- **Q：显示「接收端离线」？** A：接收端未启动或网络断开。程序每 3 秒自动重连，恢复后自动显示在线。
- **Q：播报没有声音？** A：检查系统音量与「终端设置-主音量」；音色选不到中文语音时，确认系统已安装中文语音包。
- **Q：呼叫收不到？** A：检查两端房间号是否同为 6 位数字且一致，网络可访问 `wss://broker-cn.emqx.io:8084`。
- **Q：点了「×」程序还在托盘里？** A：右键任务栏右下角的喇叭图标 →「退出校园传呼」。
- **Q：右下角通知一直在？** A：通知会挂到这条呼叫播报结束才自动消失；想提前关可点通知右上角 ✕。

## 多端形态与目录结构

| 端 | 说明 |
|---|---|
| 手机版（APK） | Android WebView 应用，`com.campus.call`，minSdk 24 / targetSdk 34 |
| 电脑版（Windows） | .NET 8 + WebView2 单文件自包含 exe，绿色免安装 |
| iOS 版 | `CampusPager-iOS`，Swift + WKWebView 原生工程（与三端同源，含横幅+提示音桥接） |
| 网页版 | 单文件 `CampusApp/index.html`（本地引用 `mqtt.min.js`），浏览器直开 |
| Java 版 | `CampusCall-Java`，Java 17 + Swing + Eclipse Paho，可跨平台编译 |

```
├── CampusApp/               # 网页版（单文件 HTML + mqtt.min.js，其他端共用此页面）
├── CampusCallApp/           # Android 工程（Gradle / AGP 8.7.3 / compileSdk 34）
├── CampusCallApp-Win/       # Windows 版工程（C# WinForms + WebView2）
├── CampusCall-Java/         # Java 桌面版（Swing + Paho MQTT）
├── CampusPager-iOS/         # iOS 版工程（Swift + WKWebView，含 README-iOS.md）
├── README.md                # 使用说明（本文件）
└── CHANGELOG.md             # 版本发布说明（完整更新记录）
```

## 构建

**Android APK**
```powershell
# 需要 JDK 17 + Android SDK（android-34、build-tools 34.0.0）+ Gradle 8.9
# 在 CampusCallApp/ 下设置 local.properties: sdk.dir=...
gradlew assembleDebug
# 产物: app/build/outputs/apk/debug/app-debug.apk
```

**Windows 版**
```powershell
# 需要 .NET SDK 8
cd CampusCallApp-Win
dotnet publish -c Release -r win-x64
# 产物: bin/Release/net8.0-windows/win-x64/publish/CampusCallApp.exe
```

**Java 版**
```powershell
javac -encoding UTF-8 -cp "lib\org.eclipse.paho.client.mqttv3-1.2.5.jar" -d out src\com\campus\call\*.java
jar cfe CampusCall.jar com.campus.call.Main -C out .
```

**iOS 版**（需 macOS + Xcode）
```text
打开 CampusPager-iOS/CampusPager-iOS.xcodeproj → 选择签名团队 → 连 iPhone 真机 Cmd+R
详细步骤与平台限制见 CampusPager-iOS/README-iOS.md
```

## 技术说明

- 单一 HTML 源码四端同源：Android APK（WebView 包装）+ Windows 独立版（C# WebView2 包装）+ iOS 版（Swift WKWebView 包装）+ 网页版源码。
- 消息主题：`campus/call/房间号`（呼叫）、`campus/presence/房间号`（在线状态，retain）。
- 名单、参数、外观等设置保存在本地（localStorage）。
- MQTT 库：Eclipse Paho（JS 版 mqtt.js v5.10.1，MIT License）。

## 版本历史

完整更新记录见 [CHANGELOG.md](CHANGELOG.md)。
