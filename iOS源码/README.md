# 校园传呼 iOS 版源码

## 说明
这是 iOS 版的源码框架，基于 WKWebView 包装 HTML。

## 文件结构
- `AppDelegate.swift` — 应用入口
- `ViewController.swift` — WebView 主控制器
- `Info.plist` — 应用配置
- `index.html` — 主页面（已包含最新版）

## 如何编译
1. 用 Xcode 新建一个 iOS 项目（Swift，UIKit）
2. 把这些文件拖进项目里
3. 配置 Bundle ID 和签名
4. 编译运行

## 注意
- 需要 macOS + Xcode 环境才能编译
- 已开启 ATS 允许任意加载（因为要连 MQTT WebSocket）
- 支持横竖屏切换
