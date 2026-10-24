package com.campus.call;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

/**
 * 启动页：输入房间号 + 选择角色（手机呼叫端 / 教室大屏端）。
 */
public class RoleSelectFrame extends JFrame {

    private final JTextField roomField = new JTextField(10);
    private JTextField brokerField;

    public RoleSelectFrame() {
        super("校园呼叫播报系统");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 470);
        setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));

        JLabel title = new JLabel("校园呼叫播报系统", JLabel.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 26));
        title.setForeground(new Color(0x22d3ee));
        root.add(title, BorderLayout.NORTH);

        JPanel mid = new JPanel();
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("输入房间号，两端填写相同房间号即可互联", JLabel.CENTER);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(new Color(0x8ea3c0));
        mid.add(hint);
        mid.add(Box.createVerticalStrut(10));

        roomField.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomField.setMaximumSize(new Dimension(280, 36));
        roomField.setHorizontalAlignment(JTextField.CENTER);
        roomField.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        roomField.setText(Storage.loadSetting("room", ""));
        mid.add(roomField);
        mid.add(Box.createVerticalStrut(26));

        JButton caller = new JButton("📱 手机呼叫端（发起呼叫）");
        caller.setAlignmentX(Component.CENTER_ALIGNMENT);
        caller.setMaximumSize(new Dimension(340, 52));
        caller.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        caller.addActionListener(e -> enter("caller"));
        mid.add(caller);
        mid.add(Box.createVerticalStrut(12));

        JButton display = new JButton("🖥️ 教室大屏端（接收播报）");
        display.setAlignmentX(Component.CENTER_ALIGNMENT);
        display.setMaximumSize(new Dimension(340, 52));
        display.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        display.addActionListener(e -> enter("display"));
        mid.add(display);
        mid.add(Box.createVerticalStrut(12));

        JButton settings = new JButton("连接设置");
        settings.setAlignmentX(Component.CENTER_ALIGNMENT);
        settings.setMaximumSize(new Dimension(150, 32));
        settings.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        settings.addActionListener(e -> showSettings());
        mid.add(settings);

        root.add(mid, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void enter(String role) {
        String room = roomField.getText().trim();
        if (room.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先输入房间号", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Storage.saveSetting("room", room);
        String broker = brokerField != null ? brokerField.getText().trim() : Storage.loadSetting("broker", MqttService.DEFAULT_BROKER);
        dispose();
        new MainFrame(role, room, broker).setVisible(true);
    }

    private void showSettings() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        JLabel l = new JLabel("MQTT Broker 地址（wss:// 或 ws://）");
        l.setForeground(new Color(0x8ea3c0));
        brokerField = new JTextField(Storage.loadSetting("broker", MqttService.DEFAULT_BROKER));
        brokerField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        p.add(l, BorderLayout.NORTH);
        p.add(brokerField, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, p, "连接设置", JOptionPane.PLAIN_MESSAGE);
        if (brokerField.getText().trim().isEmpty()) {
            brokerField.setText(MqttService.DEFAULT_BROKER);
        }
    }
}
