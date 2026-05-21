package ckthltm.connection;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UI extends JFrame {
    private static final int LEFT_WIDTH = 380;
    private static final int RIGHT_WIDTH = 520;

    // ── Colors (same palette as client) ───────────
    private static final Color BG_MAIN = new Color(0xF4F7FB);
    private static final Color BG_CARD = new Color(0xFFFFFF);
    private static final Color BG_SIDEBAR = new Color(0xEAF0FB);

    private static final Color ACCENT_BLUE = new Color(0x5B8CDD);

    private static final Color TEXT_DARK = new Color(0x2C3E6B);
    private static final Color TEXT_MID = new Color(0x5A6A8A);

    private static final Color BORDER_COLOR = new Color(0xDDE4F0);
    private static final Color LOG_BG = new Color(0xF8FAFF);

    // ── Fonts ─────────────────────────────────────
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_LOG = new Font("Consolas", Font.PLAIN, 15);

    // ── UI ─────────────────────────────────────────
    private JTextArea logArea;
    private JLabel lblCanBo;
    private JLabel lblPhongThi;
    private JLabel lblCaThi;

    private PrintStream originalOut;
    private PrintStream originalErr;

    public UI() {
        super("TCP Server");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 700);
        setMinimumSize(new Dimension(850, 600));
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
    }

    public void display() {
        Runnable show = () -> {
            setVisible(true);
            installSystemStreamRedirect();
            appendLog("[SERVER UI] Started");
        };

        if (SwingUtilities.isEventDispatchThread()) {
            show.run();
        } else {
            SwingUtilities.invokeLater(show);
        }
    }

    public void setStats(int canBoCount, int phongThiCount, int caThiCount) {
        SwingUtilities.invokeLater(() -> {
            lblCanBo.setText(String.valueOf(canBoCount));
            lblPhongThi.setText(String.valueOf(phongThiCount));
            lblCaThi.setText(String.valueOf(caThiCount));
        });
    }

    // ── Layout ────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT_BLUE);
        header.setBorder(new EmptyBorder(18, 28, 18, 28));

        JLabel title = new JLabel("TCP Server");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Server lắng nghe và xử lý yêu cầu từ client");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(0xD6E4FF));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
        center.setBackground(BG_MAIN);
        center.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel left = buildStatsPanel();
        fixPanelWidth(left, LEFT_WIDTH);

        JPanel right = buildLogPanel();
        fixPanelWidth(right, RIGHT_WIDTH);

        center.add(left);
        center.add(Box.createHorizontalStrut(14));
        center.add(right);

        return center;
    }

    private JPanel buildStatsPanel() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.add(sectionLabel("Thông tin hệ thống"), BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(3, 1, 0, 10));
        rows.setOpaque(false);

        lblCanBo = valueLabel("-");
        lblPhongThi = valueLabel("-");
        lblCaThi = valueLabel("-");

        rows.add(labeledValue("Số cán bộ tối đa", lblCanBo));
        rows.add(labeledValue("Số phòng thi", lblPhongThi));
        rows.add(labeledValue("Số ca thi đã tạo", lblCaThi));

        card.add(rows, BorderLayout.CENTER);

        wrap.add(card);
        wrap.add(Box.createVerticalGlue());
        return wrap;
    }

    private JPanel buildLogPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(0, 10));
        panel.add(sectionLabel("Nhật ký hoạt động"), BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(FONT_LOG);
        logArea.setBackground(LOG_BG);
        logArea.setForeground(TEXT_DARK);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── Style helpers (from client style) ─────────
    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        return card;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    private JLabel valueLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_VALUE);
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    private JPanel labeledValue(String labelText, JLabel valueLabel) {
        JPanel wrap = new JPanel(new BorderLayout(0, 4));
        wrap.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MID);

        JPanel valueWrap = new JPanel(new BorderLayout());
        valueWrap.setBackground(BG_SIDEBAR);
        valueWrap.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(10, 12, 10, 12)));
        valueWrap.add(valueLabel, BorderLayout.WEST);

        wrap.add(lbl, BorderLayout.NORTH);
        wrap.add(valueWrap, BorderLayout.CENTER);
        return wrap;
    }

    private void fixPanelWidth(JComponent component, int width) {
        Dimension pref = component.getPreferredSize();
        int preferredHeight = pref != null ? pref.height : 0;
        component.setPreferredSize(new Dimension(width, preferredHeight));
        component.setMinimumSize(new Dimension(width, 0));
        component.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        component.setAlignmentY(Component.TOP_ALIGNMENT);
    }

    // ── Logging ───────────────────────────────────
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            if (logArea == null) {
                return;
            }
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + time + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void installSystemStreamRedirect() {
        if (originalOut != null || originalErr != null) {
            return;
        }

        originalOut = System.out;
        originalErr = System.err;

        PrintStream redirectedOut = new PrintStream(new TextAreaOutputStream(), true, StandardCharsets.UTF_8);
        PrintStream redirectedErr = new PrintStream(new TextAreaOutputStream(), true, StandardCharsets.UTF_8);
        System.setOut(redirectedOut);
        System.setErr(redirectedErr);

        appendLog("[SERVER UI] Attached System.out/System.err");
    }

    private class TextAreaOutputStream extends OutputStream {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public synchronized void write(int b) throws IOException {
            char c = (char) b;
            buffer.append(c);
            if (c == '\n') {
                flushBuffer();
            }
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) throws IOException {
            String s = new String(b, off, len, StandardCharsets.UTF_8);
            buffer.append(s);
            int idx;
            while ((idx = buffer.indexOf("\n")) >= 0) {
                String line = buffer.substring(0, idx);
                buffer.delete(0, idx + 1);
                appendLog(line);
            }
        }

        @Override
        public synchronized void flush() throws IOException {
            flushBuffer();
        }

        private void flushBuffer() {
            if (buffer.length() == 0) {
                return;
            }
            String text = buffer.toString();
            buffer.setLength(0);
            String line = text.endsWith("\n") ? text.substring(0, text.length() - 1) : text;
            if (!line.isEmpty()) {
                appendLog(line);
            }
        }
    }
}
