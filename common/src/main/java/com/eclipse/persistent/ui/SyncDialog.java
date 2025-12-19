package com.eclipse.persistent.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.awt.image.BufferedImage;

public class SyncDialog {

    private static boolean result = false;

    public static boolean showConflictDialog() {
        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Persistent Options Sync");
                frame.setUndecorated(true);
                frame.setSize(500, 280);
                frame.setLocationRelativeTo(null);
                frame.setAlwaysOnTop(true);
                frame.setAutoRequestFocus(true);

                Color bgDark = new Color(32, 32, 32);
                Color textWhite = new Color(235, 235, 235);
                Color textGray = new Color(170, 170, 170);
                Color btnGreen = new Color(40, 167, 69);
                Color btnRed = new Color(220, 53, 69);

                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBackground(bgDark);
                mainPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));

                JLabel title = new JLabel("Settings Conflict Detected", SwingConstants.CENTER);
                title.setFont(new Font("Segoe UI", Font.BOLD, 22));
                title.setForeground(textWhite);
                title.setBorder(new EmptyBorder(30, 0, 10, 0));
                mainPanel.add(title, BorderLayout.NORTH);

                JLabel message = new JLabel(
                        "<html><div style='text-align: center; width: 350px;'>" +
                                "We found existing settings in this instance folder.<br><br>" +
                                "Do you want to <font color='#81c784'><b>import them</b></font> to your global config, " +
                                "or <font color='#e57373'><b>delete them</b></font> and use your global defaults?" +
                                "</div></html>"
                );
                message.setHorizontalAlignment(SwingConstants.CENTER);
                message.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                message.setForeground(textGray);
                mainPanel.add(message, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
                buttonPanel.setBackground(bgDark);
                buttonPanel.setBorder(new EmptyBorder(10, 0, 35, 0));

                JButton btnImport = createModernButton("Import (Use Local)", btnGreen, Color.WHITE);
                JButton btnDelete = createModernButton("Delete (Use Global)", btnRed, Color.WHITE);

                btnImport.addActionListener(e -> {
                    result = true;
                    closeFrame(frame, latch);
                });

                btnDelete.addActionListener(e -> {
                    result = false;
                    closeFrame(frame, latch);
                });

                buttonPanel.add(btnImport);
                buttonPanel.add(btnDelete);
                mainPanel.add(buttonPanel, BorderLayout.SOUTH);

                frame.add(mainPanel);
                setWindowIcon(frame);

                frame.setVisible(true);
                frame.toFront();
                frame.requestFocus();

            } catch (Exception e) {
                result = false;
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }

    private static void closeFrame(JFrame frame, CountDownLatch latch) {
        frame.dispose();
        latch.countDown();
    }

    private static JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setUI(new BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private static void setWindowIcon(JFrame frame) {
        try {
            InputStream stream = SyncDialog.class.getResourceAsStream("/assets/persistent_options/icon.png");
            if (stream != null) {
                BufferedImage icon = ImageIO.read(stream);
                frame.setIconImage(icon);
                if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    Taskbar.getTaskbar().setIconImage(icon);
                }
            }
        } catch (Exception ignored) {}
    }
}