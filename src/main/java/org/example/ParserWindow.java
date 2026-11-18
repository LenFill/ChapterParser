package org.example;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.concurrent.ExecutionException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class ParserWindow {
    public static void show() {
        JFrame window = new JFrame("Парсер глав");
        window.setSize(900, 650);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        JTextField urlField = new JTextField();
        JButton loadButton = new JButton("Загрузить");
        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(loadButton, BorderLayout.EAST);
        JSpinner volumeSpinner =
                new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        JSpinner firstChapterSpinner =
                new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        JSpinner lastChapterSpinner =
                new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        JPanel rangePanel = new JPanel();
        rangePanel.add(new JLabel("Том:"));
        rangePanel.add(volumeSpinner);
        rangePanel.add(new JLabel("С главы:"));
        rangePanel.add(firstChapterSpinner);
        rangePanel.add(new JLabel("По главу:"));
        rangePanel.add(lastChapterSpinner);

        topPanel.add(rangePanel, BorderLayout.SOUTH);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        window.add(topPanel, BorderLayout.NORTH);
        window.add(new JScrollPane(textArea), BorderLayout.CENTER);
        loadButton.addActionListener(event ->
                loadChapter(window, urlField, loadButton, textArea,
                        volumeSpinner, firstChapterSpinner, lastChapterSpinner));
        JButton saveButton = new JButton("Сохранить TXT");
        window.add(saveButton, BorderLayout.SOUTH);
        saveButton.addActionListener(event ->
                saveChapter(window, textArea));
        window.setVisible(true);
    }
    private static void loadChapter(
            JFrame window,
            JTextField urlField,
            JButton loadButton,
            JTextArea textArea,
            JSpinner volumeSpinner,
            JSpinner firstChapterSpinner,
            JSpinner lastChapterSpinner
    ) {
        String url = urlField.getText().trim();

        if (url.isBlank()) {
            JOptionPane.showMessageDialog(window, "Введите ссылку на главу.");
            return;
        }
        int volume = (Integer) volumeSpinner.getValue();
        int firstChapter = (Integer) firstChapterSpinner.getValue();
        int lastChapter = (Integer) lastChapterSpinner.getValue();

        if (lastChapter < firstChapter) {
            JOptionPane.showMessageDialog(
                    window, "Последняя глава должна быть не меньше первой."
            );
            return;
        }
        loadButton.setEnabled(false);
        textArea.setText("Загрузка...");
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return ChapterParser.parseChapters(
                        url, volume, firstChapter, lastChapter
                );
            }
            @Override
            protected void done() {
                try {
                    textArea.setText(get());
                    textArea.setCaretPosition(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    textArea.setText("Загрузка прервана.");
                } catch (ExecutionException e) {
                    textArea.setText("Не удалось загрузить главу.");
                    JOptionPane.showMessageDialog(
                            window,
                            e.getCause().toString(),
                            "Ошибка загрузки",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    loadButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
    private static void saveChapter(JFrame window, JTextArea textArea) {
        String text = textArea.getText();

        if (text.isBlank() || text.equals("Загрузка...")
                || text.equals("Не удалось загрузить главу.")
                || text.equals("Загрузка прервана.")) {
            JOptionPane.showMessageDialog(window, "Сначала загрузите главу.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить главу");
        chooser.setFileFilter(new FileNameExtensionFilter("Текстовый файл (*.txt)", "txt"));
        chooser.setSelectedFile(new java.io.File("chapter.txt"));

        if (chooser.showSaveDialog(window) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase(java.util.Locale.ROOT)
                .endsWith(".txt")) {
            path = path.resolveSibling(path.getFileName() + ".txt");
        }

        if (Files.exists(path)) {
            int answer = JOptionPane.showConfirmDialog(
                    window,
                    "Файл уже существует. Заменить?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            Files.writeString(path, text, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(window, "Глава сохранена.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    window,
                    "Не удалось сохранить файл: " + e.getMessage(),
                    "Ошибка сохранения",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
