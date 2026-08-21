package com.eclipse.persistent.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class OldVersionWarningDialog {

    public static void showDialog() {
        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Persistent Options Warning");
                frame.setUndecorated(true);
                frame.setSize(600, 350);
                frame.setLocationRelativeTo(null);
                frame.setAutoRequestFocus(true);

                Color bgDark = new Color(32, 32, 32);
                Color textWhite = new Color(235, 235, 235);
                Color textGray = new Color(170, 170, 170);
                Color btnGreen = new Color(40, 167, 69);

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
                                "You have used an <font color='#e57373'><b>older version</b></font> of the mod " +
                                "since installing the new version.<br><br>" +
                                "Make sure you have updated the mod in <i>all instances</i> that use it, as with coming updates " +
                                "the mod will no longer support older formats, and will not sync properly with older versions." +
                                "</div></html>"
                );
                message.setHorizontalAlignment(SwingConstants.CENTER);
                message.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                message.setForeground(textGray);
                mainPanel.add(message, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
                buttonPanel.setBackground(bgDark);
                buttonPanel.setBorder(new EmptyBorder(10, 0, 35, 0));

                JButton btnOk = createButton("Okay", btnGreen, Color.WHITE);

                btnOk.addActionListener(e -> {
                    closeFrame(frame, latch);
                });

                buttonPanel.add(btnOk);
                mainPanel.add(buttonPanel, BorderLayout.SOUTH);

                frame.add(mainPanel);
                setWindowIcon(frame);

                frame.setVisible(true);
                frame.toFront();
                frame.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void closeFrame(JFrame frame, CountDownLatch latch) {
        frame.dispose();
        latch.countDown();
    }

    private static JButton createButton(String text, Color bg, Color fg) {
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
            InputStream stream = OldVersionWarningDialog.class.getResourceAsStream("/assets/persistent_options/icon.png");
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