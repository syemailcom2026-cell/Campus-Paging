import UIKit
import AudioToolbox

/// 顶部横幅：呼叫已发出 / 收到呼叫时从屏幕顶部滑入，挂到呼叫结束（HTML 发 "done"）自动收起，
/// 也可点右上角 ✕ 手动关闭。
final class BannerView: UIView {

    var onClose: (() -> Void)?

    private let label = UILabel()
    private let closeBtn = UIButton(type: .system)

    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = UIColor(red: 0.11, green: 0.15, blue: 0.23, alpha: 0.97)
        layer.cornerRadius = 14
        layer.borderWidth = 1
        layer.borderColor = UIColor(red: 0.13, green: 0.20, blue: 0.30, alpha: 1).cgColor
        layer.shadowColor = UIColor.black.cgColor
        layer.shadowOpacity = 0.35
        layer.shadowOffset = CGSize(width: 0, height: 4)
        layer.shadowRadius = 10

        label.font = UIFont.systemFont(ofSize: 15, weight: .semibold)
        label.textColor = UIColor(red: 0.91, green: 0.93, blue: 0.99, alpha: 1)
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false
        addSubview(label)

        closeBtn.setTitle("✕", for: .normal)
        closeBtn.setTitleColor(UIColor(red: 0.54, green: 0.58, blue: 0.65, alpha: 1), for: .normal)
        closeBtn.titleLabel?.font = UIFont.systemFont(ofSize: 15, weight: .bold)
        closeBtn.translatesAutoresizingMaskIntoConstraints = false
        closeBtn.addTarget(self, action: #selector(tappedClose), for: .touchUpInside)
        addSubview(closeBtn)

        NSLayoutConstraint.activate([
            label.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 14),
            label.topAnchor.constraint(equalTo: topAnchor, constant: 10),
            label.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -10),
            label.trailingAnchor.constraint(lessThanOrEqualTo: closeBtn.leadingAnchor, constant: -10),
            closeBtn.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -10),
            closeBtn.centerYAnchor.constraint(equalTo: centerYAnchor),
            closeBtn.widthAnchor.constraint(equalToConstant: 28),
            closeBtn.heightAnchor.constraint(equalToConstant: 28)
        ])
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    func setMessage(_ text: String) {
        label.text = text.isEmpty ? "校园传呼" : text
    }

    @objc private func tappedClose() {
        onClose?()
    }
}
