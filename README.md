# 校园传呼 · Campus Paging

校园呼叫播报工具 —— 手机发送端一键呼叫，教室大屏（电脑）接收端实时大字显示 + 语音播报。

- **版本：1.6.2**
- 消息经 MQTT 公共服务器转发，主题 `campus/call/房间号`，无需注册、无需自建服务器。

## 功能特性

### 📡 实时通信
- 基于 MQTT 协议的即时消息推送，低延迟
- 房间号隔离，支持多班级/多区域同时使用
- 在线状态实时检测，接收端离线自动提醒
- 房间号冲突检测，防止多人同时占用同一房间

### 📢 播报能力
- 浏览器原生语音合成（TTS），无需额外安装
- 可调语速、音调、音量、播报字号
- 支持重复播放遍数和间隔设置
- 接收端支持小窗通知 / 强制大屏两种弹窗模式
- 大屏播报结束自动退回后台

### 🎨 个性化外观
- 8 种预设 UI 配色方案，支持自定义主色
- 深色/浅色模式自动跟随系统
- 支持自定义背景图片，可调背景透明度、卡片透明度
- 可自定义大胶囊圆角、左右边距、底部间距
- 底部胶囊形悬浮 Tab 栏，大圆角卡片设计
- 宽屏自动两列布局，手机端单列手风琴设置

### 📋 呼叫管理
- 多选学生批量呼叫
- 常用目的地自定义管理（教务处、办公室、医务室等）
- 呼叫记录自动保存，支持一键清空
- 常用播报模板快速选择

## 下载

| 平台 | 文件 | 说明 |
| --- | --- | --- |
| Android 手机 | [CampusPager-v1.6.2.apk](https://github.com/syemailcom2026-cell/Campus-Paging/releases/download/v1.6.2/CampusPager-v1.6.2.apk) | 直接安装（需允许安装未知来源应用） |
| Windows 电脑 | [CampusPager-Standalone.exe](https://github.com/syemailcom2026-cell/Campus-Paging/releases/download/v1.6.2/CampusPager-Standalone.exe) | 绿色免安装，双击即用（即「独立版」，GitHub 不支持文件名中的中文，故用英文命名） |
| 网页版（免安装） | https://syemailcom2026-cell.github.io/Campus-Paging/ | 浏览器直接打开即用，手机 / 电脑均可 |

## v1.6.2 更新内容
- 修复设置页手风琴布局间距不统一问题
- 修复 Tab 切换时设置页内容叠在其他页面的 bug
- 修复选择背景图按钮无响应问题
- 修复背景透明度、卡片透明度滑块无效问题
- 优化自定义配色按钮样式
- 记录页空状态居中显示
- 添加白底绿色喇叭应用图标
- 新增大胶囊圆角、左右边距、底部间距自定义调节滑块

GitHub Release：https://github.com/syemailcom2026-cell/Campus-Paging/releases （历史版本均保留）


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
- 终端设置：主音量、播报音色、弹出方式（小窗 / 大屏）、测试播报、停止、收起窗口。

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
