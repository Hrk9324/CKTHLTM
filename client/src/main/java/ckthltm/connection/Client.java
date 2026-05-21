package ckthltm.connection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import ckthltm.interfaces.MessageCallback;
import ckthltm.interfaces.SendMessageCallback;

public class Client extends JFrame {

    // ── Colors ─────────────────────────────────────
    private static final Color BG_MAIN = new Color(0xF4F7FB);
    private static final Color BG_CARD = new Color(0xFFFFFF);
    private static final Color BG_SIDEBAR = new Color(0xEAF0FB);

    private static final Color ACCENT_BLUE = new Color(0x5B8CDD);
    private static final Color ACCENT_MINT = new Color(0x4DBFA8);
    private static final Color ACCENT_SOFT = new Color(0x8FA8D8);

    private static final Color TEXT_DARK = new Color(0x2C3E6B);
    private static final Color TEXT_MID = new Color(0x5A6A8A);
    private static final Color TEXT_LIGHT = new Color(0x9BAABE);

    private static final Color BORDER_COLOR = new Color(0xDDE4F0);

    private static final Color LOG_BG = new Color(0xF8FAFF);

    // ── Fonts (tăng size) ──────────────────────────
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);

    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);

    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 15);

    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 16);

    private static final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 15);

    private static final Font FONT_LOG = new Font("Consolas", Font.PLAIN, 15);

    // ── Networking ─────────────────────────────────
    private final String fileSavePath = "downloads";

    private MessageSender messageSender = new MessageSender();

    private Socket connectedSocket = null;

    // ── UI ─────────────────────────────────────────
    private JTextArea logArea;

    private JTextField txtN;
    private JTextField txtM;

    private JTextField txtSelectedFile;

    private File selectedFile;

    // ───────────────────────────────────────────────
    public Client() {

        super("TCP Client");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(950, 700);

        setMinimumSize(new Dimension(850, 600));

        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        root.setBackground(BG_MAIN);

        root.add(buildHeader(), BorderLayout.NORTH);

        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);

        setVisible(true);
    }

    // ── Connect ────────────────────────────────────
    public void connect(InetAddress address, int port) {

        new Thread(() -> {

            try {

                connectedSocket = new Socket(address, port);

                appendLog(
                        "Đã kết nối server "
                                + address.getHostAddress()
                                + ":" + port);

                MessageListener listener = new MessageListener(
                        connectedSocket,
                        fileSavePath,
                        new ReceivedCallback());

                listener.start();

            } catch (IOException e) {

                                appendLog("Không thể kết nối: "
                        + e.getMessage());
            }

        }).start();
    }

    // ── Receive callback ───────────────────────────
    private class ReceivedCallback
            implements MessageCallback {

        @Override
        public void onTextMessageReceived(String message) {

            appendLog("Server: " + message);
        }

        @Override
        public void onFileMessageReceived(
                String savedPath,
                String fileName) {

            appendLog(
                    "Đã nhận file: "
                            + fileName);
        }
    }

    // ── Send callback ──────────────────────────────
    private class SendCallback
            implements SendMessageCallback {

        @Override
        public void sendMessage(String message) {

            if (connectedSocket != null) {

                messageSender.sendMessage(
                        connectedSocket,
                        message);
            }
        }

        @Override
        public void sendFile(
                String filePath,
                String fileName) {

            if (connectedSocket != null) {

                messageSender.sendFile(
                        connectedSocket,
                        filePath,
                        fileName);
            }
        }
    }

    // ── Header ─────────────────────────────────────
    private JPanel buildHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(ACCENT_BLUE);

        header.setBorder(
                new EmptyBorder(18, 28, 18, 28));

        JLabel title = new JLabel("TCP Client");

        title.setFont(FONT_TITLE);

        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel(
                "Client gửi và nhận dữ liệu với server");

        sub.setFont(
                new Font("Segoe UI",
                        Font.PLAIN,
                        14));

        sub.setForeground(new Color(0xD6E4FF));

        JPanel left = new JPanel();

        left.setOpaque(false);

        left.setLayout(
                new BoxLayout(
                        left,
                        BoxLayout.Y_AXIS));

        left.add(title);

        left.add(Box.createVerticalStrut(4));

        left.add(sub);

        header.add(left, BorderLayout.WEST);

        return header;
    }

    // ── Center ─────────────────────────────────────
    private JPanel buildCenter() {

        JPanel center = new JPanel(new GridBagLayout());

        center.setBackground(BG_MAIN);

        center.setBorder(
                new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;

        gbc.insets = new Insets(0, 0, 0, 14);

        // left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.48;
        gbc.weighty = 1.0;

        center.add(buildLeftPanel(), gbc);

        // right
        gbc.gridx = 1;
        gbc.weightx = 0.52;
        gbc.insets = new Insets(0, 0, 0, 0);

        center.add(buildLogPanel(), gbc);

        return center;
    }

    // ── Left panel ─────────────────────────────────
    private JPanel buildLeftPanel() {

        JPanel panel = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS));

        panel.add(buildMessageCard());

        panel.add(Box.createVerticalStrut(14));

        panel.add(buildFileCard());

        return panel;
    }

    // ── Message card ───────────────────────────────
    private JPanel buildMessageCard() {

        JPanel card = createCard();

        card.setLayout(
                new BorderLayout(0, 14));

        card.add(
                sectionLabel("Gửi tham số"),
                BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 1, 0, 10));

        fields.setOpaque(false);

        txtN = styledField("Nhập n...");
        txtM = styledField("Nhập m...");

        fields.add(
                labeledField("Giá trị n", txtN));

        fields.add(
                labeledField("Giá trị m", txtM));

        JButton btnSend = roundButton(
                "Gửi yêu cầu",
                ACCENT_BLUE,
                Color.WHITE);

        btnSend.setPreferredSize(
                new Dimension(0, 44));

        btnSend.addActionListener(
                e -> sendMessage());

        card.add(fields, BorderLayout.CENTER);

        card.add(btnSend, BorderLayout.SOUTH);

        return card;
    }

    // ── File card ──────────────────────────────────
    private JPanel buildFileCard() {

        JPanel card = createCard();

        card.setLayout(
                new BorderLayout(0, 12));

        card.add(
                sectionLabel("Gửi file"),
                BorderLayout.NORTH);

        JPanel choosePanel = new JPanel(new BorderLayout(10, 0));

        choosePanel.setBackground(BG_SIDEBAR);

        choosePanel.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                BORDER_COLOR,
                                1,
                                true),
                        new EmptyBorder(
                                10,
                                12,
                                10,
                                12)));

        txtSelectedFile = new JTextField();

        txtSelectedFile.setEditable(false);

        txtSelectedFile.setText("Chưa chọn file");

        txtSelectedFile.setFont(FONT_LABEL);

        txtSelectedFile.setForeground(TEXT_LIGHT);

        txtSelectedFile.setBackground(BG_SIDEBAR);

        txtSelectedFile.setBorder(null);

        txtSelectedFile.setPreferredSize(
                new Dimension(220, 24));

        txtSelectedFile.setMinimumSize(
                new Dimension(220, 24));

        txtSelectedFile.setMaximumSize(
                new Dimension(220, 24));

        JButton btnChoose = roundButton(
                "Chọn",
                ACCENT_SOFT,
                Color.WHITE);

        btnChoose.setPreferredSize(
                new Dimension(90, 42));

        btnChoose.setMinimumSize(
                new Dimension(90, 42));

        btnChoose.setMaximumSize(
                new Dimension(90, 42));

        btnChoose.addActionListener(
                e -> chooseFile());

        choosePanel.add(
                txtSelectedFile,
                BorderLayout.CENTER);

        choosePanel.add(
                btnChoose,
                BorderLayout.EAST);

        JButton btnSend = roundButton(
                "Gửi file",
                ACCENT_MINT,
                Color.WHITE);

        btnSend.setPreferredSize(
                new Dimension(0, 44));

        btnSend.addActionListener(
                e -> sendFile());

        card.add(choosePanel, BorderLayout.CENTER);

        card.add(btnSend, BorderLayout.SOUTH);

        return card;
    }

    // ── Log panel ──────────────────────────────────
    private JPanel buildLogPanel() {

        JPanel panel = createCard();

        panel.setLayout(
                new BorderLayout(0, 10));

        panel.add(
                sectionLabel("Nhật ký hoạt động"),
                BorderLayout.NORTH);

        logArea = new JTextArea();

        logArea.setEditable(false);

        logArea.setFont(FONT_LOG);

        logArea.setBackground(LOG_BG);

        logArea.setForeground(TEXT_DARK);

        logArea.setLineWrap(true);

        logArea.setWrapStyleWord(true);

        logArea.setBorder(
                new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(logArea);

        scroll.setBorder(
                new LineBorder(
                        BORDER_COLOR,
                        1,
                        true));

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── Send message ───────────────────────────────
    private void sendMessage() {

        String nStr = txtN.getText().trim();

        String mStr = txtM.getText().trim();

        if (nStr.isEmpty() || mStr.isEmpty()) {

                        appendLog("Cảnh báo: Vui lòng nhập n và m");

            return;
        }

        try {

            int n = Integer.parseInt(nStr);

            int m = Integer.parseInt(mStr);

            String message = "n=" + n + ", m=" + m;

            new SendCallback().sendMessage(message);

            appendLog("Client: " + message);

        } catch (NumberFormatException e) {

                        appendLog("Cảnh báo: n và m phải là số nguyên");
        }
    }
    // ── Choose file ────────────────────────────────

    private void chooseFile() {

        JFileChooser fc = new JFileChooser();

        fc.setFileFilter(
                new FileNameExtensionFilter(
                        "Excel Files (*.xlsx)",
                        "xlsx"));

        int result = fc.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            selectedFile = fc.getSelectedFile();

            txtSelectedFile.setText(
                    selectedFile.getName());

            txtSelectedFile.setForeground(
                    TEXT_DARK);

            txtSelectedFile.setCaretPosition(0);

            appendLog(
                    "Đã chọn file: "
                            + selectedFile.getName());
        }
    }

    // ── Send file ──────────────────────────────────
    private void sendFile() {

        if (selectedFile == null) {

                        appendLog("Cảnh báo: Chưa chọn file");

            return;
        }

        new Thread(() -> {

            try {

                new SendCallback().sendFile(
                        selectedFile.getAbsolutePath(),
                        selectedFile.getName());

                appendLog(
                        "Đã gửi file: "
                                + selectedFile.getName());

            } catch (Exception e) {

                appendLog(
                        "Lỗi gửi file: "
                                + e.getMessage());
            }

        }).start();
    }

    // ── Helpers ────────────────────────────────────
    private JPanel createCard() {

        JPanel card = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BG_CARD);

                g2.fill(
                        new RoundRectangle2D.Float(
                                0,
                                0,
                                getWidth(),
                                getHeight(),
                                16,
                                16));

                g2.dispose();
            }
        };

        card.setOpaque(false);

        card.setBorder(
                new EmptyBorder(
                        18,
                        18,
                        18,
                        18));

        return card;
    }

    private JLabel sectionLabel(String text) {

        JLabel lbl = new JLabel(text);

        lbl.setFont(FONT_SECTION);

        lbl.setForeground(TEXT_DARK);

        return lbl;
    }

    private JPanel labeledField(
            String labelText,
            JTextField field) {

        JPanel wrap = new JPanel(new BorderLayout(0, 4));

        wrap.setOpaque(false);

        JLabel lbl = new JLabel(labelText);

        lbl.setFont(FONT_LABEL);

        lbl.setForeground(TEXT_MID);

        wrap.add(lbl, BorderLayout.NORTH);

        wrap.add(field, BorderLayout.CENTER);

        return wrap;
    }

    private JTextField styledField(String placeholder) {

        JTextField field = new JTextField();

        field.setFont(FONT_INPUT);

        field.setForeground(TEXT_DARK);

        field.setBackground(BG_SIDEBAR);

        field.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                BORDER_COLOR,
                                1,
                                true),
                        new EmptyBorder(
                                10,
                                12,
                                10,
                                12)));

        field.setPreferredSize(
                new Dimension(0, 44));

        return field;
    }

    private JButton roundButton(
            String text,
            Color bg,
            Color fg) {

        JButton btn = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(bg);

                g2.fill(
                        new RoundRectangle2D.Float(
                                0,
                                0,
                                getWidth(),
                                getHeight(),
                                10,
                                10));

                g2.dispose();

                super.paintComponent(g);
            }
        };

        btn.setFont(FONT_BTN);

        btn.setForeground(fg);

        btn.setOpaque(false);

        btn.setContentAreaFilled(false);

        btn.setBorderPainted(false);

        btn.setFocusPainted(false);

        btn.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));

        return btn;
    }

    // ── Log ────────────────────────────────────────
    private void appendLog(String message) {

        SwingUtilities.invokeLater(() -> {

            String time = LocalTime.now().format(
                    DateTimeFormatter.ofPattern(
                            "HH:mm:ss"));

            logArea.append(
                    "[" + time + "] "
                            + message
                            + "\n");

            logArea.setCaretPosition(
                    logArea.getDocument()
                            .getLength());
        });
    }
}