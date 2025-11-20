package org.github.son_nik.crystaldata.controller;

import org.github.son_nik.crystaldata.model.*;
import org.github.son_nik.crystaldata.view.MainFrame;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class MainController {
    private MainFrame view;
    private ParsedData currentData;
    private File currentFile;

    public MainController(MainFrame view) {
        this.view = view;
        setupListeners();
    }

    private void setupListeners() {
        view.setOpenButtonListener(new OpenFileListener());
        view.setSaveButtonListener(new SaveFileListener());
    }

    private class OpenFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.setStatusText("Opening file...");

            int returnValue = view.getFileChooser().showOpenDialog(view);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                currentFile = view.getFileChooser().getSelectedFile();
                if (currentFile.getName().toLowerCase().endsWith(".lis")) {
                    parseFile(currentFile);
                    view.setStatusText("File opened successfully");
                } else {
                    view.showMessage("Please select a .lis file");
                    view.setStatusText("Ready");
                }
            } else {
                view.setStatusText("Ready");
            }
        }
    }

    private class SaveFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.setStatusText("Saving file...");

            if (currentData == null) {
                view.showMessage("No data to save. Please open a file first.");
                view.setStatusText("Ready");
                return;
            }

            int returnValue = view.getFileChooser().showSaveDialog(view);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File saveFile = view.getFileChooser().getSelectedFile();
                try (PrintWriter writer = new PrintWriter(saveFile)) {
                    writer.write(view.getResultText());
                    view.showMessage("File saved successfully!");
                    view.setStatusText("File saved successfully");
                } catch (IOException ex) {
                    view.showMessage("Error saving file: " + ex.getMessage());
                    view.setStatusText("Error saving file");
                }
            } else {
                view.setStatusText("Ready");
            }
        }
    }

    private void parseFile(File file) {
        try {
            view.setStatusText("Parsing file...");
            List<String> lines = readFile(file);

            List<RingInteraction> ringInteractions = parseRingInteractions(lines);
            List<PiInteraction> piInteractions = parsePiInteractions(lines);

            currentData = new ParsedData(ringInteractions, piInteractions);
            view.setResultText(currentData.getFormattedResults());
            view.setStatusText("Parsing completed");

        } catch (IOException ex) {
            view.showMessage("Error reading file: " + ex.getMessage());
            view.setStatusText("Error reading file");
        }
    }

    private List<String> readFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<RingInteraction> parseRingInteractions(List<String> lines) {
        List<RingInteraction> interactions = new ArrayList<>();
        boolean inTable = false;
        Set<String> uniqueCgCg = new HashSet<>();
        int linesAfterHeader = 0;

        for (String line : lines) {
            if (line.contains("Analysis of Short Ring-Interactions with Cg-Cg Distances")) {
                inTable = true;
                linesAfterHeader = 0;
                continue;
            }

            if (inTable) {
                linesAfterHeader++;

                // Пропускаем заголовки таблицы (первые 2-3 строки после заголовка)
                if (linesAfterHeader <= 2 || line.trim().isEmpty() ||
                        line.contains("Cg(I) Res(I)") || line.contains("---")) {
                    continue;
                }

                // Если встретили новую секцию, выходим
                if (line.contains("Analysis of") && linesAfterHeader > 3) {
                    inTable = false;
                    continue;
                }

                // Извлекаем все числа из строки
                List<String> numbers = extractNumbers(line);
                if (numbers.size() >= 11) {
                    // ИСПРАВЛЕНЫ ИНДЕКСЫ на основе отладочной информации:
                    // Cg-Cg теперь индекс 1 (вместо 0)
                    // Alpha теперь индекс 5 (вместо 4)
                    // CgI_Perp теперь индекс 8 (правильно)
                    String cgCg = numbers.get(1);    // Было 0, теперь 1
                    String alpha = numbers.get(5);    // Было 4, теперь 5
                    String cgIPerp = numbers.get(8);  // Оставили 8

                    // Проверяем уникальность и условие Cg-Cg <= 4
                    if (uniqueCgCg.add(cgCg) && parseValue(cgCg) <= 4.0) {
                        interactions.add(new RingInteraction(cgCg, cgIPerp, alpha));
                    }
                }

                // Если строка пустая, возможно, таблица закончилась
                if (line.trim().isEmpty()) {
                    inTable = false;
                }
            }
        }

        return interactions;
    }

    private List<PiInteraction> parsePiInteractions(List<String> lines) {
        List<PiInteraction> interactions = new ArrayList<>();
        boolean inTable = false;
        Set<String> uniqueXCg = new HashSet<>();
        int linesAfterHeader = 0;

        for (String line : lines) {
            if (line.contains("Analysis of Y-X...Cg(Pi-Ring) Interactions")) {
                inTable = true;
                linesAfterHeader = 0;
                continue;
            }

            if (inTable) {
                linesAfterHeader++;

                // Пропускаем заголовки таблицы (первые 2-3 строки после заголовка)
                if (linesAfterHeader <= 2 || line.trim().isEmpty() ||
                        line.contains("Y--X(I)") || line.contains("---")) {
                    continue;
                }

                // Если встретили новую секцию, выходим
                if (line.contains("Analysis of") && linesAfterHeader > 3) {
                    inTable = false;
                    continue;
                }

                // Извлекаем все числа из строки
                List<String> numbers = extractNumbers(line);
                if (numbers.size() >= 11) {
                    // ИСПРАВЛЕНЫ ИНДЕКСЫ на основе отладочной информации:
                    // X..Cg теперь индекс 1 (вместо 0)
                    // X-Perp теперь индекс 6 (вместо 5)
                    // Y-X..Cg теперь индекс 8 (вместо 6)
                    String xCg = numbers.get(1);    // Было 0, теперь 1
                    String xPerp = numbers.get(6);   // Было 5, теперь 6
                    String yxCg = numbers.get(8);    // Было 6, теперь 8

                    // Проверяем уникальность
                    if (uniqueXCg.add(xCg)) {
                        interactions.add(new PiInteraction(xCg, xPerp, yxCg));
                    }
                }

                // Если строка пустая, возможно, таблица закончилась
                if (line.trim().isEmpty()) {
                    inTable = false;
                }
            }
        }

        return interactions;
    }

    // Метод для извлечения всех чисел из строки (включая отрицательные и числа в скобках)
    private List<String> extractNumbers(String line) {
        List<String> numbers = new ArrayList<>();
        // Регулярное выражение для чисел: может быть отрицательным, с плавающей точкой, возможно со скобками в конце
        Pattern pattern = Pattern.compile("-?\\d+\\.\\d+(\\(\\d+\\))?");
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }

    private double parseValue(String value) {
        // Убираем скобки для преобразования в число
        if (value.contains("(")) {
            value = value.substring(0, value.indexOf('('));
        }
        return Double.parseDouble(value);
    }
}