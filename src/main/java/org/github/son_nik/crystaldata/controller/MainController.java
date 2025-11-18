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
            int returnValue = view.getFileChooser().showOpenDialog(view);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                currentFile = view.getFileChooser().getSelectedFile();
                if (currentFile.getName().toLowerCase().endsWith(".lis")) {
                    parseFile(currentFile);
                } else {
                    view.showMessage("Please select a .lis file");
                }
            }
        }
    }

    private class SaveFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentData == null) {
                view.showMessage("No data to save. Please open a file first.");
                return;
            }

            int returnValue = view.getFileChooser().showSaveDialog(view);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File saveFile = view.getFileChooser().getSelectedFile();
                try (PrintWriter writer = new PrintWriter(saveFile)) {
                    writer.write(view.getResultText());
                    view.showMessage("File saved successfully!");
                } catch (IOException ex) {
                    view.showMessage("Error saving file: " + ex.getMessage());
                }
            }
        }
    }

    private void parseFile(File file) {
        try {
            List<String> lines = readFile(file);
            List<RingInteraction> ringInteractions = parseRingInteractions(lines);
            List<PiInteraction> piInteractions = parsePiInteractions(lines);

            currentData = new ParsedData(ringInteractions, piInteractions);
            view.setResultText(currentData.getFormattedResults());

        } catch (IOException ex) {
            view.showMessage("Error reading file: " + ex.getMessage());
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

        Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\(\\d+\\))\\s+[\\d.-]+\\s+[\\d.-]+\\s+[\\d.-]+\\s+(\\d+)\\s+[\\d.]+\\s+[\\d.]+\\s+(\\d+\\.\\d+)");

        for (String line : lines) {
            if (line.contains("Analysis of Short Ring-Interactions with Cg-Cg Distances")) {
                inTable = true;
                continue;
            }

            if (inTable && line.contains("Cg(I) Res(I)")) {
                continue; // Пропускаем заголовок таблицы
            }

            if (inTable && line.trim().isEmpty()) {
                inTable = false;
                continue;
            }

            if (inTable) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String cgCg = matcher.group(1);
                    String alpha = matcher.group(2);
                    String cgIPerp = matcher.group(3);

                    // Проверяем уникальность и условие Cg-Cg <= 4
                    if (uniqueCgCg.add(cgCg) && parseValue(cgCg) <= 4.0) {
                        interactions.add(new RingInteraction(cgCg, cgIPerp, alpha));
                    }
                }
            }
        }

        return interactions;
    }

    private List<PiInteraction> parsePiInteractions(List<String> lines) {
        List<PiInteraction> interactions = new ArrayList<>();
        boolean inTable = false;
        Set<String> uniqueXCg = new HashSet<>();

        Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\(\\d+\\))\\s+[\\d.-]+\\s+[\\d.-]+\\s+[\\d.-]+\\s+([-]?\\d+\\.\\d+)\\s+[\\d.]+\\s+(\\d+\\.\\d+\\(\\d+\\))");

        for (String line : lines) {
            if (line.contains("Analysis of Y-X...Cg(Pi-Ring) Interactions")) {
                inTable = true;
                continue;
            }

            if (inTable && line.contains("Y--X(I)")) {
                continue; // Пропускаем заголовок таблицы
            }

            if (inTable && line.trim().isEmpty()) {
                inTable = false;
                continue;
            }

            if (inTable) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String xCg = matcher.group(1);
                    String xPerp = matcher.group(2);
                    String yxCg = matcher.group(3);

                    // Проверяем уникальность
                    if (uniqueXCg.add(xCg)) {
                        interactions.add(new PiInteraction(xCg, xPerp, yxCg));
                    }
                }
            }
        }

        return interactions;
    }

    private double parseValue(String value) {
        // Убираем скобки для преобразования в число
        if (value.contains("(")) {
            value = value.substring(0, value.indexOf('('));
        }
        return Double.parseDouble(value);
    }
}
