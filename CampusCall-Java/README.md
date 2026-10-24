# 校园呼叫播报系统 · Java 版

用 **Java（Swing）** 重写的桌面版，功能与原网页版（MQTT over WebSocket.html）一致：
手机呼叫端发起呼叫，教室大屏端收到后大字显示并自动语音播报。

## 运行方式

1. 确保电脑装有 **JDK 17**（本机已在 `C:\Users\LENOVO\android-dev-tools\jdk-extract\jdk-17.0.20.1+1`）；
2. 双击 **`run.bat`**（会自动查找 Java 并启动）；
3. 或命令行运行：

```powershell
& "C:\Users\LENOVO\android-dev-tools\jdk-extract\jdk-17.0.20.1+1\bin\java.exe" -Dfile.encoding=UTF-8 -cp "CampusCall.jar;lib\org.eclipse.paho.client.mqttv3-1.2.5.jar" com.campus.call.Main
```

## 功能对照

| 功能 | 说明 |
|---|---|
| 角色选择 | 手机呼叫端（发起呼叫）/ 教室大屏端（接收播报） |
| 房间号配对 | 两端输入相同房间号即互联（MQTT 主题 `campus/call/{房间号}`） |
| 发起呼叫 | 选班级 → 选学生 → 可改播报语 → 一键发送 |
| 语音播报 | 大屏端收到呼叫自动用 Windows 系统语音（SAPI）中文播报 |
| 学生名单 | 按「班级,姓名」每行一个编辑，保存在 `用户目录/.campuscall/roster.txt` |
| 呼叫记录 | 发送/接收记录保存在本机 `records.log`，可一键清空 |
| 连接设置 | 可修改 MQTT Broker 地址（默认 `wss://broker-cn.emqx.io:8084/mqtt`） |
| 断线重连 | Paho 客户端自动重连，状态栏实时显示 |

## 工程结构

```
CampusCall-Java/
├── src/com/campus/call/
│   ├── Main.java           # 入口（深色主题）
│   ├── RoleSelectFrame.java# 启动页：房间号 + 角色选择
│   ├── MainFrame.java      # 主界面（呼叫端/大屏端）
│   ├── MqttService.java    # MQTT over WebSocket（Paho）
│   ├── TtsUtil.java        # 语音播报（Windows SAPI）
│   ├── Storage.java        # 名单/设置/记录持久化
│   └── MqttSmokeTest.java  # MQTT 连通性自测（可选）
├── lib/org.eclipse.paho.client.mqttv3-1.2.5.jar
├── CampusCall.jar          # 编译好的可执行包
├── run.bat                 # 双击启动
└── out/                    # 编译输出
```

## 编译方式

```powershell
javac -encoding UTF-8 -cp "lib\org.eclipse.paho.client.mqttv3-1.2.5.jar" -d out src\com\campus\call\*.java
jar cfe CampusCall.jar com.campus.call.Main -C out .
```

## 说明

- 数据目录：`用户目录/.campuscall/`（名单 roster.txt、设置 settings.properties、记录 records.log）
- MQTT 主题格式为 `campus/call/{房间号}`；若要与旧版网页互通，需保持两端主题一致
- 语音播报使用 Windows 系统 TTS（SAPI），依赖系统自带中文语音引擎
- 跨平台：本工程在 Windows / macOS / Linux 均可编译运行（语音播报部分目前适配 Windows SAPI）
