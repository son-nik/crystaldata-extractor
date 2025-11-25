package org.github.son_nik.crystaldata.controller;

import org.github.son_nik.crystaldata.model.*;
import org.github.son_nik.crystaldata.view.MainFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

public class MainController {
    private MainFrame view;
    private ParsedData currentData;
    private File currentFile;
    private SettingsManager settingsManager;

    public MainController(MainFrame view) {
        this.view = view;
        this.settingsManager = new SettingsManager();
        setupListeners();
        initializeFileChooser();
    }

    private void initializeFileChooser() {
        File lastDir = new File(settingsManager.getLastDirectory());
        if (lastDir.exists() && lastDir.isDirectory()) {
            view.getFileChooser().setCurrentDirectory(lastDir);
        }
    }

    private void setupListeners() {
        view.setOpenButtonListener(new OpenFileListener());
        view.setSaveButtonListener(new SaveFileListener());
        view.setSettingsButtonListener(new SettingsListener());
    }

    private class OpenFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.setStatusText("Opening file...");

            int returnValue = view.getFileChooser().showOpenDialog(view);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                currentFile = view.getFileChooser().getSelectedFile();

                settingsManager.setLastDirectory(currentFile.getParent());

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


            String fileName = generateFileName();
            view.getFileChooser().setSelectedFile(new File(fileName));

            int returnValue = view.getFileChooser().showSaveDialog(view);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File saveFile = view.getFileChooser().getSelectedFile();

                if (!saveFile.getName().toLowerCase().endsWith(".txt")) {
                    saveFile = new File(saveFile.getParent(), saveFile.getName() + ".txt");
                }


                settingsManager.setLastDirectory(saveFile.getParent());

                try (PrintWriter writer = new PrintWriter(saveFile)) {
                    writer.write(view.getResultText());
                    view.showMessage("File saved successfully!");
                    view.setStatusText("File saved successfully");


                    if (settingsManager.isUseIndex()) {
                        settingsManager.setCurrentIndex(settingsManager.getCurrentIndex() + 1);
                    }
                } catch (IOException ex) {
                    view.showMessage("Error saving file: " + ex.getMessage());
                    view.setStatusText("Error saving file");
                }
            } else {
                view.setStatusText("Ready");
            }
        }

        private String generateFileName() {
            String baseName = settingsManager.getDefaultFileName();
            if (settingsManager.isUseIndex()) {
                int index = settingsManager.getCurrentIndex();
                return String.format("%s%02d.txt", baseName, index);
            } else {
                return baseName + ".txt";
            }
        }
    }

    private class SettingsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showSettingsDialog();
        }
    }

    private void showSettingsDialog() {

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        JTextField fileNameField = new JTextField(settingsManager.getDefaultFileName(), 20);
        JTextField indexField = new JTextField(String.valueOf(settingsManager.getCurrentIndex()), 5);
        JCheckBox useIndexCheckBox = new JCheckBox("Use index", settingsManager.isUseIndex());

        panel.add(new JLabel("Default file name:"));
        panel.add(fileNameField);
        panel.add(new JLabel("Current index:"));
        panel.add(indexField);
        panel.add(new JLabel(""));
        panel.add(useIndexCheckBox);

        int result = JOptionPane.showConfirmDialog(
                view,
                panel,
                "Settings",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {

                settingsManager.setDefaultFileName(fileNameField.getText().trim());
                settingsManager.setUseIndex(useIndexCheckBox.isSelected());


                if (useIndexCheckBox.isSelected()) {
                    int index = Integer.parseInt(indexField.getText().trim());
                    if (index > 0) {
                        settingsManager.setCurrentIndex(index);
                    } else {
                        view.showMessage("Index must be a positive number");
                    }
                }

                view.showMessage("Settings saved successfully!");
            } catch (NumberFormatException ex) {
                view.showMessage("Invalid index format. Please enter a valid number.");
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


                if (linesAfterHeader <= 2 || line.trim().isEmpty() ||
                        line.contains("Cg(I) Res(I)") || line.contains("---")) {
                    continue;
                }


                if (line.contains("Analysis of") && linesAfterHeader > 3) {
                    inTable = false;
                    continue;
                }


                List<String> numbers = extractNumbers(line);
                if (numbers.size() >= 11) {
                    String cgCg = numbers.get(1);
                    String alpha = numbers.get(5);
                    String cgIPerp = numbers.get(8);


                    if (uniqueCgCg.add(cgCg) && parseValue(cgCg) <= 4.0) {
                        interactions.add(new RingInteraction(cgCg, cgIPerp, alpha));
                    }
                }


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


                if (linesAfterHeader <= 2 || line.trim().isEmpty() ||
                        line.contains("Y--X(I)") || line.contains("---")) {
                    continue;
                }


                if (line.contains("Analysis of") && linesAfterHeader > 3) {
                    inTable = false;
                    continue;
                }


                List<String> numbers = extractNumbers(line);
                if (numbers.size() >= 11) {
                    String xCg = numbers.get(1);
                    String xPerp = numbers.get(6);
                    String yxCg = numbers.get(8);


                    if (uniqueXCg.add(xCg)) {
                        interactions.add(new PiInteraction(xCg, xPerp, yxCg));
                    }
                }


                if (line.trim().isEmpty()) {
                    inTable = false;
                }
            }
        }

        return interactions;
    }

    private List<String> extractNumbers(String line) {
        List<String> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("-?\\d+\\.\\d+(\\(\\d+\\))?");
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }

    private double parseValue(String value) {
        if (value.contains("(")) {
            value = value.substring(0, value.indexOf('('));
        }
        return Double.parseDouble(value);
    }
}