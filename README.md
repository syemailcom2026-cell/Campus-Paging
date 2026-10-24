# 校园传呼（Campus Paging）

一款校园呼叫播报工具：**手机端选择学生 → 指定目的地 → 一键呼叫，教室大屏端收到后大字显示并自动语音播报**。

- 两端填写相同的 **6 位数字房间号** 即可互联，无需注册、无需自建服务器；
- 消息经 MQTT 公共服务器（`wss://broker-cn.emqx.io:8084/mqtt`）转发，主题 `campus/call/{房间号}`；
- 开发团队：**symonolith** ｜ 版本 2.0.0

## 功能

### 手机端（呼叫端）
- 搜索 / 按班级筛选学生，点击即选中（呼叫详情卡片）
- 呼叫目的地：常用目的地一键选择、更多目的地展开、手动输入自定义
- 附加备注（可选），播报内容实时预览
- 发送呼叫 / 本机试听
- 四个标签页：呼叫 · 自定义（目的地管理）· 记录（呼叫记录）· 设置

### 电脑端（大屏端）
- 收到呼叫大字显示，自动语音播报，待播内容进入**播报队列**依次播报
- **终端设置**：主音量、播报音色（系统中文语音）、测试播报、停止
- 大屏在线状态上报，呼叫端顶部实时显示

### 呼叫参数（随消息下发给大屏）
重复遍数 · 重复间隔 · 语速 · 音调 · 音量，均可在呼叫端设置页调整。

### 播报模板
支持变量 `{class}` 班级 · `{name}` 姓名 · `{destination}` 目的地，模板文本可自定义，
例如：`请{class}的{name}同学，听到广播后到{destination}。`

## 多端形态

| 端 | 说明 |
|---|---|
| 手机版（APK） | Android WebView 应用，`com.campus.call`，minSdk 24 / targetSdk 34 |
| 电脑版（Windows） | .NET 8 + WebView2 单文件自包含 exe，绿色免安装 |
| 网页版 | 单文件 `CampusApp/index.html`（本地引用 `mqtt.min.js`），浏览器直开 |
| Java 版 | `CampusCall-Java`，Java 17 + Swing + Eclipse Paho，可跨平台编译 |

任意两端填写相同房间号即可互呼。

## 目录结构

```
├── CampusApp/               # 网页版（单文件 HTML + mqtt.min.js，其他端共用此页面）
├── CampusCallApp/           # Android 工程（Gradle / AGP 8.7.3 / compileSdk 34）
├── CampusCallApp-Win/       # Windows 版工程（C# WinForms + WebView2）
├── CampusCall-Java/         # Java 桌面版（Swing + Paho MQTT）
├── 使用说明.txt
└── README.md
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

## 安装包

APK 与 Windows exe 安装包通过 GitHub **Releases** 发布，见右侧 Releases 页面。

## 说明

- 公共 Broker 消息公开，请勿传输隐私信息；可在「连接设置」中换成自己的 MQTT 服务器；
- MQTT 库：Eclipse Paho（JS 版 mqtt.js v5.10.1，MIT License）。
