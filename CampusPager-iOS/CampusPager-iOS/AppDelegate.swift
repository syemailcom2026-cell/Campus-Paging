import UIKit
import AVFoundation

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // 静音模式也允许提示音
        try? AVAudioSessionShim.shared.activate()
        return true
    }

    func application(_ application: UIApplication,
                     configurationForConnecting connectingSceneSession: UISceneSession,
                     options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        let cfg = UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
        cfg.delegateClass = SceneDelegate.self
        return cfg
    }
}

/// 让系统提示音在静音开关下也能播放（横幅提示音用 AudioServicesPlaySystemSound 足够，
/// 无需完整 AVAudioSession；此处仅作兜底）。
final class AVAudioSessionShim {
    static let shared = AVAudioSessionShim()
    func activate() {
        #if targetEnvironment(simulator)
        // 模拟器无需处理
        #else
        try? AVAudioSession.sharedInstance().setCategory(.playback, options: [.mixWithOthers])
        try? AVAudioSession.sharedInstance().setActive(true)
        #endif
    }
}
