package com.campus.call;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        applyDarkTheme();
        SwingUtilities.invokeLater(() -> new RoleSelectFrame().setVisible(true));
    }

    /** 深色科技风主题（与网页版视觉一致） */
    static void applyDarkTheme() {
        UIManager.put("Panel.background", new Color(0x0b1220));
        UIManager.put("Panel.foreground", new Color(0xe8eefc));
        UIManager.put("Label.background", new Color(0x0b1220));
        UIManager.put("Label.foreground", new Color(0xe8eefc));
        UIManager.put("Button.background", new Color(0x1c2c44));
        UIManager.put("Button.foreground", new Color(0xe8eefc));
        UIManager.put("Button.select", new Color(0x22d3ee));
        UIManager.put("TextField.background", new Color(0x121c2e));
        UIManager.put("TextField.foreground", new Color(0xe8eefc));
        UIManager.put("TextField.caretForeground", new Color(0xe8eefc));
        UIManager.put("TextArea.background", new Color(0x121c2e));
        UIManager.put("TextArea.foreground", new Color(0xe8eefc));
        UIManager.put("List.background", new Color(0x121c2e));
        UIManager.put("List.foreground", new Color(0xe8eefc));
        UIManager.put("List.selectionBackground", new Color(0x0ea5e9));
        UIManager.put("List.selectionForeground", Color.WHITE);
        UIManager.put("OptionPane.background", new Color(0x0b1220));
        UIManager.put("OptionPane.messageForeground", new Color(0xe8eefc));
        UIManager.put("ScrollPane.background", new Color(0x121c2e));
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("ComboBox.background", new Color(0x121c2e));
        UIManager.put("ComboBox.foreground", new Color(0xe8eefc));
        UIManager.put("TitledBorder.titleColor", new Color(0x8ea3c0));
    }
}
