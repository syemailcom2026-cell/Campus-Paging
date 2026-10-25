import UIKit
import WebKit

/// 校园传呼 iOS 端：WKWebView 加载与 Android / Windows 同源的 index.html，
/// 通过 messageHandlers.campus 桥接 HTML 的 hostMsg / hostPopup 接口：
/// - type = "sent" | "call"  → 顶部横幅 + 提示音（呼叫已发出 / 收到呼叫）
/// - type = "done"           → 关闭对应 id 的横幅（该呼叫播报结束）
class ViewController: UIViewController, WKNavigationDelegate, WKScriptMessageHandler, WKUIDelegate {

    private var webView: WKWebView!
    private var banners: [String: BannerView] = [:]
    private var bannerTopConstraint: NSLayoutConstraint?

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(red: 0.06, green: 0.08, blue: 0.13, alpha: 1)

        let config = WKWebViewConfiguration()
        config.userContentController.add(self, name: "campus")
        if #available(iOS 14.0, *) {
            config.defaultWebpagePreferences.allowsContentJavaScript = true
        }

        webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = self
        webView.uiDelegate = self
        webView.scrollView.bounces = true
        webView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(webView)

        NSLayoutConstraint.activate([
            webView.topAnchor.constraint(equalTo: view.topAnchor),
            webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            webView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])

        if let url = Bundle.main.url(forResource: "index", withExtension: "html") {
            webView.loadFileURL(url, allowingReadAccessTo: url.deletingLastPathComponent())
        }
    }

    // MARK: - JS -> Native 桥接

    func userContentController(_ userContentController: WKUserContentController,
                               didReceive message: WKScriptMessage) {
        guard message.name == "campus",
              let body = message.body as? [String: Any] else { return }
        let type = body["type"] as? String ?? ""
        let id = body["id"] as? String ?? UUID().uuidString
        let text = body["text"] as? String ?? ""
        switch type {
        case "sent", "call":
            showBanner(id: id, text: text)
        case "done":
            dismissBanner(id: id)
        case "reply":
            // 接收端回复（知道了 / 文字回复）→ 呼叫端横幅显示
            showBanner(id: id, text: text)
        case "openUrl":
            // 1.4.2：网页版入口——用系统浏览器打开网页版
            if let urlString = body["url"] as? String,
               let url = URL(string: urlString),
               UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
            }
        default:
            break
        }
    }

    // MARK: - 横幅

    private func showBanner(id: String, text: String) {
        if let existing = banners[id] {
            existing.setMessage(text)
            return
        }
        let banner = BannerView()
        banner.setMessage(text)
        banner.onClose = { [weak self] in self?.dismissBanner(id: id) }
        banners[id] = banner

        view.addSubview(banner)
        banner.translatesAutoresizingMaskIntoConstraints = false
        let top = banner.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: -120)
        NSLayoutConstraint.activate([
            top,
            banner.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 12),
            banner.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -12)
        ])
        view.layoutIfNeeded()
        top.constant = 8
        UIView.animate(withDuration: 0.3, delay: 0, options: [.curveEaseOut], animations: {
            self.view.layoutIfNeeded()
        })
        AudioServicesPlaySystemSound(1013) // 提示音（三音）
    }

    private func dismissBanner(id: String) {
        guard let banner = banners[id] else { return }
        banners.removeValue(forKey: id)
        UIView.animate(withDuration: 0.25, animations: {
            banner.alpha = 0
        }, completion: { _ in
            banner.removeFromSuperview()
        })
    }

    // MARK: - WKNavigationDelegate

    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        print("nav error: \(error.localizedDescription)")
    }

    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        print("provisional error: \(error.localizedDescription)")
    }

    // MARK: - WKUIDelegate（alert / confirm / prompt 必需，否则 HTML 的弹窗静默失败）

    func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage message: String,
                 initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
        let ac = UIAlertController(title: "校园传呼", message: message, preferredStyle: .alert)
        ac.addAction(UIAlertAction(title: "好", style: .default) { _ in completionHandler() })
        present(ac, animated: true)
    }

    func webView(_ webView: WKWebView, runJavaScriptConfirmPanelWithMessage message: String,
                 initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (Bool) -> Void) {
        let ac = UIAlertController(title: "校园传呼", message: message, preferredStyle: .alert)
        ac.addAction(UIAlertAction(title: "取消", style: .cancel) { _ in completionHandler(false) })
        ac.addAction(UIAlertAction(title: "确定", style: .default) { _ in completionHandler(true) })
        present(ac, animated: true)
    }

    func webView(_ webView: WKWebView, runJavaScriptTextInputPanelWithPrompt prompt: String,
                 defaultText: String?, initiatedByFrame frame: WKFrameInfo,
                 completionHandler: @escaping (String?) -> Void) {
        let ac = UIAlertController(title: prompt, message: nil, preferredStyle: .alert)
        ac.addTextField { $0.text = defaultText }
        ac.addAction(UIAlertAction(title: "取消", style: .cancel) { _ in completionHandler(nil) })
        ac.addAction(UIAlertAction(title: "确定", style: .default) { _ in
            completionHandler(ac.textFields?.first?.text)
        })
        present(ac, animated: true)
    }
}
