import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.awt.geom.Rectangle2D;

public class GUI {
    // GUI组件
    private JLabel titleLabel, displayLabel, statusLabel, timeLabel;
    private JFrame mainFrame;
    private JButton callButton, manageButton, importButton, statsButton;
    private JButton skinButton,  exportButton, historyButton;
    private JComboBox<String> strategyCombo, classCombo;
    private static final String[] STRATEGIES = {"基础随机", "正确率优先", "新人优先", "均衡点名"};
    private static final String[] SKIN_COLORS = {"浅灰", "淡蓝",  "墨绿", "靛紫"};

    // 背景和皮肤
    private Color currentSkin = new Color(245, 245, 245);

    // 课堂数据
    private List<Student> calledStudents = new ArrayList<>();
    private ClassSession currentSession;
    private boolean isClassStarted = false;
    private int classSessionId = 0, unansweredCount = 0;
    private ScheduledExecutorService scheduler;

    public void initGUI() {
        setupMainFrame();
        setupTopPanel();
        setupDisplayPanel();
        setupControlPanel();
        setupEventHandlers();
        startTimeUpdater();
    }

    private void setupMainFrame() {
        mainFrame = new JFrame("课堂点名系统 V3.0 增强版");
        mainFrame.setSize(900, 650);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        JPanel contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(currentSkin.getRed(), currentSkin.getGreen(),
                        currentSkin.getBlue(), 150));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPane.setLayout(new BorderLayout(15, 15));
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainFrame.setContentPane(contentPane);
    }

    private void setupTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel = new JLabel("课堂点名系统 V3.0 增强版", JLabel.CENTER);
        titleLabel.setFont(new Font("华文楷体", Font.BOLD, 36));
        titleLabel.setForeground(new Color(50, 50, 120));

        timeLabel = new JLabel("当前时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(timeLabel, BorderLayout.EAST);

        ((JPanel)mainFrame.getContentPane()).add(topPanel, BorderLayout.NORTH);
    }

    private void setupDisplayPanel() {
        JPanel displayPanel = new JPanel(new BorderLayout()); // 改为普通面板
        displayPanel.setOpaque(false); // 设置透明

        statusLabel = new JLabel("状态: 未开始上课 | 已点名: 0人", JLabel.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));

        displayLabel = new JLabel("准备开始上课...", JLabel.CENTER);
        displayLabel.setFont(new Font("微软雅黑", Font.BOLD, 60));
        displayLabel.setBorder(null); // 移除边框

        displayPanel.add(statusLabel, BorderLayout.NORTH);
        displayPanel.add(displayLabel, BorderLayout.CENTER);

        mainFrame.getContentPane().add(displayPanel, BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(3, 5, 15, 15));
        controlPanel.setOpaque(false);

        strategyCombo = new JComboBox<>(STRATEGIES);
        strategyCombo.setSelectedIndex(0);
        strategyCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        classCombo = new JComboBox<>();
        classCombo.addItem("当前课堂");
        for (ClassSession session : DataManager.classHistory) {
            classCombo.addItem(session.getSummary());
        }
        classCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        callButton = createStyledButton("开始上课/点名", new Color(100, 150, 255));
        manageButton = createStyledButton("管理学生", new Color(100, 180, 200));
        importButton = createStyledButton("导入名单", new Color(120, 160, 220));
        statsButton = createStyledButton("数据可视化", new Color(140, 180, 240));
        skinButton = createStyledButton("切换皮肤", new Color(160, 200, 220));

        exportButton = createStyledButton("导出数据", new Color(200, 180, 160));
        historyButton = createStyledButton("课堂历史", new Color(220, 160, 140));

        controlPanel.add(new JLabel("点名策略:", SwingConstants.RIGHT));
        controlPanel.add(strategyCombo);
        controlPanel.add(new JLabel("课堂选择:", SwingConstants.RIGHT));
        controlPanel.add(classCombo);
        controlPanel.add(new JLabel());

        controlPanel.add(callButton);
        controlPanel.add(manageButton);
        controlPanel.add(importButton);
        controlPanel.add(statsButton);
        controlPanel.add(skinButton);

        controlPanel.add(exportButton);
        controlPanel.add(historyButton);
        controlPanel.add(new JLabel());
        controlPanel.add(new JLabel());

        ((JPanel)mainFrame.getContentPane()).add(controlPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bgColor.darker()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    private void setupEventHandlers() {
        callButton.addActionListener(e -> handleNameCalling());
        manageButton.addActionListener(e -> showManageDialog());
        importButton.addActionListener(e -> importStudents());
        statsButton.addActionListener(e -> showStatistics());
        skinButton.addActionListener(e -> showSkinDialog());
        exportButton.addActionListener(e -> exportData());
        historyButton.addActionListener(e -> showClassHistory());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DataManager.saveData();
            if (scheduler != null) scheduler.shutdown();
        }));
    }

    private void startTimeUpdater() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(() -> {
                timeLabel.setText("当前时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            });
        }, 0, 1, TimeUnit.SECONDS);

        mainFrame.setVisible(true);
    }

    private void handleNameCalling() {
        if (!isClassStarted) {
            startNewClass();
            return;
        }

        if (DataManager.students.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "学生名单为空，请先添加学生！", "操作提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student selected = selectStudent();
        calledStudents.add(selected);
        selected.incrementCallCount();
        currentSession.incrementCalled(selected.getName());

        displayLabel.setText(selected.getName());
        updateStatusLabel();
        showAnswerResultDialog(selected);
    }

    private Student selectStudent() {
        String strategy = (String) strategyCombo.getSelectedItem();
        Strategy strategyImpl;
        switch (strategy) {
            case "正确率优先": strategyImpl = new CorrectRateStrategy(); break;
            case "新人优先": strategyImpl = new NewcomerStrategy(); break;
            case "均衡点名": strategyImpl = new BalancedStrategy(); break;
            default: strategyImpl = new RandomStrategy();
        }
        return strategyImpl.selectStudent(DataManager.students, calledStudents, unansweredCount);
    }

    private void startNewClass() {
        isClassStarted = true;
        classSessionId = new Random().nextInt(90000) + 10000;
        currentSession = new ClassSession(classSessionId, DataManager.students.size());
        DataManager.classHistory.add(currentSession);
        classCombo.addItem(currentSession.getSummary());

        callButton.setText("随机点名");
        displayLabel.setText("课堂ID: " + classSessionId);
        statusLabel.setText("状态: 上课中 | 已点名: 0人");
        calledStudents.clear();
        unansweredCount = 0;

        JOptionPane.showMessageDialog(mainFrame, "课堂已开始，ID: " + classSessionId, "系统提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAnswerResultDialog(Student student) {
        JDialog dialog = new JDialog(mainFrame, "回答结果", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 200);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel msgLabel = new JLabel(
                "<html><div style='text-align: center;'>" + student.getName() + "同学的回答是否正确?<br>" +
                        "历史表现: 点名" + student.getCallCount() + "次, 正确率" +
                        String.format("%.1f%%", student.getCorrectRate() * 100) + "</div></html>",
                JLabel.CENTER);
        msgLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));

        JButton correctBtn = new JButton("正确");
        correctBtn.setBackground(new Color(100, 200, 100));
        correctBtn.setForeground(Color.WHITE);
        correctBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        correctBtn.setPreferredSize(new Dimension(100, 40));

        JButton incorrectBtn = new JButton("错误");
        incorrectBtn.setBackground(new Color(200, 100, 100));
        incorrectBtn.setForeground(Color.WHITE);
        incorrectBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        incorrectBtn.setPreferredSize(new Dimension(100, 40));

        JButton skipBtn = new JButton("跳过");
        skipBtn.setBackground(new Color(150, 150, 150));
        skipBtn.setForeground(Color.WHITE);
        skipBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        skipBtn.setPreferredSize(new Dimension(100, 40));

        btnPanel.add(correctBtn);
        btnPanel.add(incorrectBtn);
        btnPanel.add(skipBtn);

        panel.add(msgLabel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(panel);

        correctBtn.addActionListener(e -> {
            student.incrementCorrectCount();
            student.recordSession(classSessionId, true);
            currentSession.incrementCorrect();
            unansweredCount = 0;
            dialog.dispose();
            updateStatusLabel();
        });

        incorrectBtn.addActionListener(e -> {
            student.resetConsecutive();
            student.recordSession(classSessionId, false);
            unansweredCount++;
            dialog.dispose();
            updateStatusLabel();
        });

        skipBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void updateStatusLabel() {
        statusLabel.setText(String.format("状态: 上课中 | 已点名: %d人 | 正确率: %.1f%%",
                calledStudents.size(), currentSession.correctCount * 100.0 / currentSession.calledCount));
    }

    private void showManageDialog() {
        JDialog dialog = new JDialog(mainFrame, "学生名单管理", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(mainFrame);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        DataManager.students.forEach(s -> listModel.addElement(s.toString()));

        JList<String> nameList = new JList<>(listModel);
        nameList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(nameList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("学生名单"));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JTextField nameField = new JTextField(20);
        JButton addBtn = new JButton("添加");
        JButton batchAddBtn = new JButton("批量添加");
        JButton removeBtn = new JButton("删除");
        JButton editBtn = new JButton("编辑");
        JButton exportBtn = new JButton("导出名单");
        JButton closeBtn = new JButton("关闭");

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入学生姓名", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (DataManager.isStudentExists(name)) {
                JOptionPane.showMessageDialog(dialog, "该学生已存在", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DataManager.students.add(new Student(name));
            listModel.addElement(DataManager.students.get(DataManager.students.size() - 1).toString());
            nameField.setText("");
        });

        batchAddBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(dialog, "请输入学生姓名，用逗号分隔");
            if (input == null || input.trim().isEmpty()) return;

            String[] names = input.split(",");
            int added = 0;
            for (String name : names) {
                String trimmed = name.trim();
                if (!trimmed.isEmpty() && !DataManager.isStudentExists(trimmed)) {
                    DataManager.students.add(new Student(trimmed));
                    listModel.addElement(DataManager.students.get(DataManager.students.size() - 1).toString());
                    added++;
                }
            }
            JOptionPane.showMessageDialog(dialog, "成功添加 " + added + " 名学生", "结果", JOptionPane.INFORMATION_MESSAGE);
        });

        removeBtn.addActionListener(e -> {
            int[] selected = nameList.getSelectedIndices();
            if (selected.length == 0) {
                JOptionPane.showMessageDialog(dialog, "请先选择要删除的学生", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(dialog,
                    "确定要删除选中的 " + selected.length + " 名学生吗？", "确认删除", JOptionPane.YES_NO_OPTION)) return;

            for (int i = selected.length - 1; i >= 0; i--) {
                String name = listModel.getElementAt(selected[i]).split(" \\(")[0];
                DataManager.students.removeIf(s -> s.getName().equals(name));
                listModel.remove(selected[i]);
            }
        });

        editBtn.addActionListener(e -> {
            int index = nameList.getSelectedIndex();
            if (index == -1) {
                JOptionPane.showMessageDialog(dialog, "请先选择要编辑的学生", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String currentName = listModel.getElementAt(index).split(" \\(")[0];
            String newName = JOptionPane.showInputDialog(dialog, "请输入新的学生姓名", currentName);
            if (newName == null || newName.trim().isEmpty() || newName.equals(currentName)) return;
            if (DataManager.isStudentExists(newName)) {
                JOptionPane.showMessageDialog(dialog, "该学生已存在", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Student student = DataManager.students.stream()
                    .filter(s -> s.getName().equals(currentName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("学生不存在"));
            student.name = newName;
            listModel.set(index, student.toString());
        });

        exportBtn.addActionListener(e -> exportStudentList());
        closeBtn.addActionListener(e -> dialog.dispose());

        controlPanel.add(new JLabel("学生姓名:"));
        controlPanel.add(nameField);
        controlPanel.add(addBtn);
        controlPanel.add(batchAddBtn);
        controlPanel.add(removeBtn);
        controlPanel.add(editBtn);
        controlPanel.add(exportBtn);
        controlPanel.add(closeBtn);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(controlPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void exportStudentList() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出学生名单");
        fileChooser.setSelectedFile(new File("学生名单_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv"));

        if (fileChooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("姓名,点名次数,正确次数,正确率,连续正确");
            for (Student student : DataManager.students) {
                writer.println(String.format("%s,%d,%d,%.2f%%,%d",
                        student.getName(), student.getCallCount(), student.getCorrectCount(),
                        student.getCorrectRate() * 100, student.consecutiveCorrect));
            }
            JOptionPane.showMessageDialog(mainFrame, "导出成功\n" + file.getAbsolutePath(),
                    "导出结果", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "导出失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importStudents() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择学生名单文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV文件 (*.csv)", "csv"));

        if (fileChooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int added = 0, duplicate = 0, invalid = 0;
            reader.readLine(); // 跳过表头

            while ((line = reader.readLine()) != null) {
                String name = line.split(",")[0].trim();
                if (name.isEmpty()) {
                    invalid++;
                    continue;
                }
                if (DataManager.isStudentExists(name)) {
                    duplicate++;
                } else {
                    DataManager.students.add(new Student(name));
                    added++;
                }
            }
            JOptionPane.showMessageDialog(mainFrame,
                    "导入完成\n新增: " + added + " 人\n重复: " + duplicate + " 人\n无效: " + invalid + " 行",
                    "导入结果", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "导入失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStatistics() {
        if (DataManager.students.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "没有学生数据可供统计！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "数据可视化 - 点名统计", true);
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(mainFrame);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("点名次数统计", createBarChartPanel());
        tabbedPane.addTab("正确率分析", createPieChartPanel());

        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }

    private JPanel createBarChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int margin = 60, width = getWidth() - 2 * margin, height = getHeight() - 2 * margin;
                g2.setColor(Color.BLACK);
                g2.drawLine(margin, margin, margin, margin + height);
                g2.drawLine(margin, margin + height, margin + width, margin + height);

                int maxCalls = DataManager.students.stream().mapToInt(Student::getCallCount).max().orElse(1);
                int step = Math.max(1, maxCalls / 10);
                for (int i = 0; i <= maxCalls; i += step) {
                    int y = margin + height - (i * height / maxCalls);
                    g2.drawLine(margin - 5, y, margin, y);
                    g2.drawString(String.valueOf(i), margin - 40, y + 5);
                }

                List<Student> topStudents = DataManager.students.stream()
                        .sorted(Comparator.comparingInt(Student::getCallCount).reversed())
                        .limit(10)
                        .collect(Collectors.toList());

                if (topStudents.isEmpty()) return;

                int barWidth = width / (topStudents.size() + 1);
                for (int i = 0; i < topStudents.size(); i++) {
                    Student student = topStudents.get(i);
                    int barHeight = (int) (student.getCallCount() * 1.0 / maxCalls * height);

                    Color barColor = new Color(100 + i * 15, 150, 200);
                    g2.setColor(barColor);
                    g2.fillRect(margin + (i + 1) * barWidth - barWidth / 2,
                            margin + height - barHeight, barWidth - 10, barHeight);

                    g2.setColor(Color.BLACK);
                    String valueText = String.format("%d (%.1f%%)",
                            student.getCallCount(), student.getCorrectRate() * 100);
                    Rectangle2D bounds = g2.getFontMetrics().getStringBounds(valueText, g2);
                    g2.drawString(valueText,
                            (float) (margin + (i + 1) * barWidth - bounds.getWidth() / 2),
                            margin + height - barHeight - 5);

                    g2.drawString(student.getName(),
                            margin + (i + 1) * barWidth - barWidth / 2,
                            margin + height + 20);
                }
            }
        };
    }

    private JPanel createPieChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int margin = 80, width = getWidth() - 2 * margin, height = getHeight() - 2 * margin;
                int centerX = margin + width / 2, centerY = margin + height / 2;
                int diameter = Math.min(width, height);

                int totalCalls = DataManager.students.stream().mapToInt(Student::getCallCount).sum();
                int correctCalls = DataManager.students.stream().mapToInt(Student::getCorrectCount).sum();
                int incorrectCalls = totalCalls == 0 ? 0 : totalCalls - correctCalls;

                g2.setColor(new Color(100, 200, 100));
                g2.fillArc(centerX - diameter/2, centerY - diameter/2, diameter, diameter, 0,
                        totalCalls == 0 ? 0 : (int) (360 * correctCalls / (double) totalCalls));

                g2.setColor(new Color(200, 100, 100));
                g2.fillArc(centerX - diameter/2, centerY - diameter/2, diameter, diameter,
                        totalCalls == 0 ? 0 : (int) (360 * correctCalls / (double) totalCalls),
                        totalCalls == 0 ? 0 : 360 - (int) (360 * correctCalls / (double) totalCalls));

                g2.setColor(Color.BLACK);
                g2.drawString("正确回答: " + correctCalls + " (" +
                                (totalCalls > 0 ? String.format("%.1f%%", correctCalls * 100.0 / totalCalls) : "0.0%"),
                        centerX - 120, centerY - 30);

                g2.drawString("错误回答: " + incorrectCalls + " (" +
                                (totalCalls > 0 ? String.format("%.1f%%", incorrectCalls * 100.0 / totalCalls) : "0.0%"),
                        centerX - 120, centerY + 30);
            }
        };
    }

    private void showSkinDialog() {
        JDialog dialog = new JDialog(mainFrame, "皮肤中心", true);
        dialog.setSize(300, 350);
        dialog.setLayout(new GridLayout(0, 1, 10, 10));
        dialog.setLocationRelativeTo(mainFrame);

        JPanel previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(280, 80));
        previewPanel.setBorder(BorderFactory.createTitledBorder("预览"));

        JPanel colorPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        colorPanel.setBorder(BorderFactory.createTitledBorder("选择皮肤"));

        for (String skin : SKIN_COLORS) {
            JButton btn = new JButton(skin);
            btn.addActionListener(e -> {
                switch (skin) {
                    case "淡蓝": currentSkin = new Color(220, 230, 255); break;
                    case "墨绿": currentSkin = new Color(200, 230, 200); break;
                    case "靛紫": currentSkin = new Color(230, 220, 250); break;
                    default: currentSkin = new Color(245, 245, 245);
                }

                mainFrame.repaint();
                dialog.dispose();
            });
            colorPanel.add(btn);
        }

        JButton resetBtn = new JButton("恢复默认");
        resetBtn.addActionListener(e -> {
            currentSkin = new Color(245, 245, 245);
            mainFrame.repaint();
            dialog.dispose();
        });

        dialog.add(previewPanel);
        dialog.add(colorPanel);
        dialog.add(resetBtn);

        dialog.setVisible(true);
    }

    private void showClassHistory() {
        if (DataManager.classHistory.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "暂无课堂历史记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "课堂历史记录", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(mainFrame);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        DataManager.classHistory.forEach(session -> listModel.addElement(session.getSummary()));

        JList<String> historyList = new JList<>(listModel);
        historyList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && historyList.getSelectedIndex() != -1) {
                ClassSession session = DataManager.classHistory.get(historyList.getSelectedIndex());
                JOptionPane.showMessageDialog(dialog, session.getDetails(), "课堂详情", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyList);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出数据");
        fileChooser.setSelectedFile(new File("课堂数据_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt"));

        if (fileChooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("===== 学生数据 =====");
            writer.println("姓名,点名次数,正确次数,正确率,连续正确");
            for (Student student : DataManager.students) {
                writer.println(String.format("%s,%d,%d,%.2f%%,%d",
                        student.getName(), student.getCallCount(), student.getCorrectCount(),
                        student.getCorrectRate() * 100, student.consecutiveCorrect));
            }

            writer.println("\n===== 课堂历史 =====");
            writer.println("课堂ID,日期,学生总数,已点名,正确次数,正确率");
            for (ClassSession session : DataManager.classHistory) {
                writer.println(String.format("%d,%s,%d,%d,%d,%.2f%%",
                        session.sessionId, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(session.date),
                        session.totalStudents, session.calledCount, session.correctCount,
                        session.calledCount > 0 ? session.correctCount * 100.0 / session.calledCount : 0));
            }

            JOptionPane.showMessageDialog(mainFrame, "数据导出成功\n" + file.getAbsolutePath(),
                    "导出结果", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "导出失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}