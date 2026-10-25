# 校园传呼 · iOS 版（CampusPager for iOS）

同款同功能的 iOS 应用：与 Android / Windows 版共用同一份 HTML 真源（`CampusApp/index.html`），
用原生 **WKWebView** 包装，并补齐 iOS 原生桥接（呼叫横幅 + 提示音）。

版本：**1.4.2**（与 Android / Windows 三端同版本同功能）

---

## 目录结构

```
CampusPager-iOS/
├── CampusPager-iOS.xcodeproj/        # Xcode 工程
└── CampusPager-iOS/
    ├── AppDelegate.swift             # 应用入口（音频会话兜底）
    ├── SceneDelegate.swift           # 场景入口
    ├── ViewController.swift          # WKWebView + JS 桥接 + alert/confirm/prompt 支持
    ├── BannerView.swift              # 顶部横幅（呼叫弹出 / 可手动关闭）
    ├── index.html                    # 与 Android / Windows 同源的真源副本（1.4.2）
    ├── mqtt.min.js                   # MQTT 客户端库（本地引用）
    ├── Info.plist                    # 显示名"校园传呼"、版本 1.4.2、ATS 配置
    ├── Assets.xcassets/              # AppIcon 全套尺寸（含 1024 App Store）
    └── Base.lproj/LaunchScreen.storyboard
```

## 功能（与三端完全一致）

- 呼叫端：班级 / 学生 / 目的地选择、多选呼叫、模板变量模块（点按 / 拖动）、自定义播报预览、下拉刷新、随机房间号
- 接收端（大屏）：收到呼叫大字显示 + 本地语音播报（iOS 内置 `speechSynthesis`），播报结束 5 秒后回退
- 呼叫已发出 / 收到呼叫：**屏幕顶部横幅**（原生 UI，非网页元素）+ 提示音，可手动 ✕ 关闭；播报结束自动收起
- 设置：呼叫参数、个性化（深色模式 / 8 套配色 / 自定义背景图）、连接、关于（版本信息与开发团队）
- MQTT 走 `wss://broker-cn.emqx.io:8084/mqtt`（TLS，不受 ATS 拦截）

## iOS 桥接约定（HTML 侧已内置）

`index.html` 的 `hostMsg()` 检测到 `window.webkit.messageHandlers.campus` 时走 iOS 通道：

| JS 消息（type） | iOS 原生行为 |
|---|---|
| `sent`（呼叫已发出） | 顶部横幅 + 提示音 |
| `call`（收到呼叫） | 顶部横幅 + 提示音 |
| `done`（呼叫结束） | 自动收起对应横幅 |

`alert / confirm / prompt` 已通过 `WKUIDelegate` 原生实现，网页弹窗正常。

## 在 Mac 上编译运行（Windows 本机无法编译 iOS）

1. 准备一台 **macOS（Xcode 15 或更高）**，安装 Xcode。
2. 打开工程：
   ```
   双击 CampusPager-iOS.xcodeproj（或 File → Open…）
   ```
3. **签名**（真机必须）：
   - 打开工程 → 选中 `CampusPager` Target → Signing & Capabilities
   - 选择你的 Apple ID 团队（需免费 Apple ID 即可真机调试）
   - Bundle ID 默认 `com.symonolith.campus-pager`（可自行修改）
4. 真机运行：连接 iPhone → 选择设备 → Cmd+R。
   - 首次运行需在 iPhone「设置 → 通用 → VPN 与设备管理」信任开发者证书。
5. 构建安装包（.ipa）：
   - 模拟器构建：Product → Build（选 Generic iOS Device 时构建 ipa 需签名）
   - 真机分发：Product → Archive → Distribute App（Ad Hoc / TestFlight / App Store）
   - 免费账号只能本机签名调试，无法上架；上架需付费 Apple Developer Program（99 美元/年）。

## 平台限制（与 Android / Windows 的差异，iOS 系统特性）

| 能力 | iOS 现状 |
|---|---|
| 前台收到广播弹横幅 | ✅ 原生横幅 + 提示音 |
| 后台持续接收广播 | ⚠️ 受限：iOS 会挂起后台应用，WebSocket 无法驻留。切后台后如需收到呼叫提醒，需接入 **APNs 推送**（需服务器端转发，属后续扩展）；目前切后台时横幅不保证弹出 |
| 语音播报 | ✅ 前台可用（speechSynthesis）；后台播报同样受系统限制 |
| 退出按钮 | iOS 不允许程序化退出，页面内"退出"仅重置回大厅 |

## 后续可选扩展（如需要可以继续做）

- APNs 远程推送：由一台常驻服务器（或云端函数）订阅 `campus/call/<房间>` 转发 APNs，实现后台提醒
- 使用 Capacitor 方案替代纯 WKWebView（如需推送插件等生态）

---

版本历史见仓库根目录 `CHANGELOG.md`；三端共用说明见 `README.md`。
