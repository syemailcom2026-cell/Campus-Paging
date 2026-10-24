package com.campus.call;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 主界面。
 * 手机呼叫端：班级/学生列表 + 播报内容 + 发起呼叫。
 * 教室大屏端：大字号显示呼叫内容 + 自动语音播报。
 */
public class MainFrame extends JFrame {

    private final String role;
    private final String room;
    private final String broker;

    private MqttService mqtt;
    private JLabel statusLabel;

    // 呼叫端组件
    private JList<String> classList;
    private JList<String> studentList;
    private JTextField speechField;
    private JButton callButton;

    // 大屏端组件
    private JLabel bigClsLabel;
    private JLabel bigNameLabel;
    private JLabel bigTextLabel;

    // 共用
    private JTextArea recordArea;

    public MainFrame(String role, String room, String broker) {
        super("校园呼叫播报系统 · " + ("caller".equals(role) ? "手机呼叫端" : "教室大屏端"));
        this.role = role;
        this.room = room;
        this.broker = broker;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 660);
        setLocationRelativeTo(null);
        buildUi();
        connectMqtt();
    }

    // ---------- 布局 ----------

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add("caller".equals(role) ? buildCallerPanel() : buildDisplayPanel(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        bar.setBackground(new Color(0x0d1526));
        JLabel title = new JLabel("校园呼叫播报系统");
        title.setFont(new Font("微软雅黑", Font.BOLD, 16));
        title.setForeground(new Color(0x22d3ee));
        JLabel roomL = new JLabel("房间号：" + room);
        roomL.setForeground(new Color(0xe8eefc));
        statusLabel = new JLabel("连接中…");
        statusLabel.setOpaque(true);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBackground(new Color(0xf59e0b));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        bar.add(title);
        bar.add(roomL);
        bar.add(statusLabel);
        return bar;
    }

    private JPanel buildCallerPanel() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        classList = new JList<>(new DefaultListModel<>());
        classList.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        classList.addListSelectionListener(e -> loadStudents());
        JScrollPane classScroll = new JScrollPane(classList);
        classScroll.setPreferredSize(new Dimension(210, 0));
        JPanel classPanel = panelWithTitle("班级", classScroll);

        studentList = new JList<>(new DefaultListModel<>());
        studentList.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        studentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    fillSpeechFromSelection();
                }
            }
        });
        JScrollPane stuScroll = new JScrollPane(studentList);
        JPanel stuPanel = panelWithTitle("学生（双击自动生成播报语）", stuScroll);

        JPanel right = new JPanel(new BorderLayout(0, 10));
        JPanel op = new JPanel();
        op.setLayout(new BoxLayout(op, BoxLayout.Y_AXIS));
        op.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        speechField = new JTextField();
        speechField.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        speechField.setToolTipText("播报内容，可手动修改");
        callButton = new JButton("📢 发起呼叫");
        callButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        callButton.setBackground(new Color(0x0ea5e9));
        callButton.setForeground(Color.WHITE);
        callButton.addActionListener(e -> doCall());
        JButton rosterBtn = new JButton("✏️ 编辑名单");
        rosterBtn.addActionListener(e -> editRoster());
        JButton clearBtn = new JButton("清空记录");
        clearBtn.addActionListener(e -> clearRecords());

        op.add(speechField);
        op.add(Box.createVerticalStrut(8));
        op.add(callButton);
        op.add(Box.createVerticalStrut(8));
        op.add(rosterBtn);
        op.add(Box.createVerticalStrut(8));
        op.add(clearBtn);
        op.setPreferredSize(new Dimension(230, 0));

        recordArea = new JTextArea();
        recordArea.setEditable(false);
        recordArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        JScrollPane recScroll = new JScrollPane(recordArea);
        recScroll.setPreferredSize(new Dimension(230, 0));
        JPanel recPanel = panelWithTitle("呼叫记录", recScroll);

        right.add(op, BorderLayout.NORTH);
        right.add(recPanel, BorderLayout.CENTER);

        p.add(classPanel, BorderLayout.WEST);
        p.add(stuPanel, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);

        loadRosterToLists();
        loadHistory();
        return p;
    }

    private JPanel buildDisplayPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(0x0b1220));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.insets = new Insets(6, 10, 6, 10);

        bigClsLabel = new JLabel("等待呼叫…", JLabel.CENTER);
        bigClsLabel.setFont(new Font("微软雅黑", Font.BOLD, 42));
        bigClsLabel.setForeground(new Color(0x67e8f9));
        bigNameLabel = new JLabel("", JLabel.CENTER);
        bigNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 60));
        bigNameLabel.setForeground(Color.WHITE);
        bigTextLabel = new JLabel("", JLabel.CENTER);
        bigTextLabel.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        bigTextLabel.setForeground(new Color(0xc7d6ee));

        g.gridy = 0;
        center.add(bigClsLabel, g);
        g.gridy = 1;
        center.add(bigNameLabel, g);
        g.gridy = 2;
        center.add(bigTextLabel, g);

        recordArea = new JTextArea();
        recordArea.setEditable(false);
        recordArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        JScrollPane recScroll = new JScrollPane(recordArea);
        recScroll.setPreferredSize(new Dimension(0, 160));

        p.add(center, BorderLayout.CENTER);
        p.add(panelWithTitle("呼叫记录", recScroll), BorderLayout.SOUTH);
        loadHistory();
        return p;
    }

    private void loadHistory() {
        for (String l : Storage.loadRecords()) {
            recordArea.append(l + "\n");
        }
        if (recordArea.getDocument().getLength() > 0) {
            recordArea.setCaretPosition(recordArea.getDocument().getLength());
        }
    }

    // 简化：直接以组件构建标题面板
    private JPanel panelWithTitle(String title, JScrollPane comp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        JLabel l = new JLabel(title);
        l.setForeground(new Color(0x8ea3c0));
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    // ---------- 名单 ----------

    private void loadRosterToLists() {
        List<String> roster = Storage.loadRoster();
        Set<String> classes = new LinkedHashSet<>();
        for (String line : roster) {
            int idx = line.indexOf(',');
            if (idx > 0) {
                classes.add(line.substring(0, idx).trim());
            }
        }
        DefaultListModel<String> m = new DefaultListModel<>();
        for (String c : classes) {
            m.addElement(c);
        }
        classList.setModel(m);
        if (m.size() > 0) {
            classList.setSelectedIndex(0);
        }
    }

    private void loadStudents() {
        String cls = classList.getSelectedValue();
        DefaultListModel<String> m = new DefaultListModel<>();
        if (cls != null) {
            for (String line : Storage.loadRoster()) {
                int idx = line.indexOf(',');
                if (idx > 0 && line.substring(0, idx).trim().equals(cls)) {
                    m.addElement(line.substring(idx + 1).trim());
                }
            }
        }
        studentList.setModel(m);
    }

    private void fillSpeechFromSelection() {
        String cls = classList.getSelectedValue();
        String name = studentList.getSelectedValue();
        if (name == null) {
            return;
        }
        speechField.setText("请" + (cls == null ? "" : cls) + name + "同学到办公室");
    }

    // ---------- 呼叫 ----------

    private void doCall() {
        String cls = classList.getSelectedValue();
        String name = studentList.getSelectedValue();
        if (name == null) {
            JOptionPane.showMessageDialog(this, "请先选择学生", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String speech = speechField.getText().trim();
        if (speech.isEmpty()) {
            speech = "请" + (cls == null ? "" : cls) + name + "同学到办公室";
        }
        String payload = MqttService.buildCallPayload(cls == null ? "" : cls, name, speech);
        try {
            mqtt.publish(payload);
            addRecord("已发送 → " + speech);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "发送失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editRoster() {
        String text = String.join("\n", Storage.loadRoster());
        JTextArea ta = new JTextArea(text, 14, 40);
        ta.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane sp = new JScrollPane(ta);
        int r = JOptionPane.showConfirmDialog(this, sp, "编辑名单（每行：班级,姓名）",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            Storage.saveRoster(ta.getText());
            loadRosterToLists();
        }
    }

    private void clearRecords() {
        int r = JOptionPane.showConfirmDialog(this, "确定清空所有呼叫记录？", "清空记录",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            Storage.clearRecords();
            recordArea.setText("");
        }
    }

    private void addRecord(String line) {
        Storage.appendRecord(line);
        SwingUtilities.invokeLater(() -> {
            recordArea.append(line + "\n");
            recordArea.setCaretPosition(recordArea.getDocument().getLength());
        });
    }

    // ---------- MQTT ----------

    private void connectMqtt() {
        mqtt = new MqttService();
        setStatus("连接中…", new Color(0xf59e0b));
        Thread t = new Thread(() -> {
            try {
                mqtt.connect(broker, room, new MqttService.Listener() {
                    @Override
                    public void onConnected() {
                        setStatus("已连接", new Color(0x22c55e));
                    }

                    @Override
                    public void onDisconnected() {
                        setStatus("已断开（自动重连中）", new Color(0xef4444));
                    }

                    @Override
                    public void onMessage(String topic, String payload) {
                        SwingUtilities.invokeLater(() -> handleMessage(payload));
                    }
                });
            } catch (Exception ex) {
                setStatus("连接失败：" + ex.getMessage(), new Color(0xef4444));
            }
        }, "mqtt-connect");
        t.setDaemon(true);
        t.start();
    }

    private void handleMessage(String payload) {
        String cls = MqttService.jsonField(payload, "cls");
        String name = MqttService.jsonField(payload, "name");
        String text = MqttService.jsonField(payload, "text");

        StringBuilder line = new StringBuilder("收到呼叫：");
        if (cls != null && !cls.isEmpty()) {
            line.append(cls).append(" ");
        }
        if (name != null && !name.isEmpty()) {
            line.append(name);
        }
        if (text != null && !text.isEmpty()) {
            line.append(" —— ").append(text);
        }
        addRecord(line.toString());

        if ("display".equals(role)) {
            bigClsLabel.setText(cls == null || cls.isEmpty() ? "呼叫" : cls);
            bigNameLabel.setText(name == null || name.isEmpty() ? "" : name);
            bigTextLabel.setText(text == null ? "" : text);
            if (text != null && !text.isEmpty()) {
                TtsUtil.speak(text);
            }
        }
    }

    private void setStatus(String s, Color c) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(s);
            statusLabel.setBackground(c);
        });
    }

    @Override
    public void dispose() {
        if (mqtt != null) {
            mqtt.disconnect();
        }
        super.dispose();
    }
}
