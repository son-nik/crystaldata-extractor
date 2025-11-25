package org.github.son_nik.crystaldata.controller;

import java.io.*;
import java.util.Properties;

public class SettingsManager {
    private static final String SETTINGS_FILE = "cdextractor_settings.properties";
    private static final String DEFAULT_FILE_NAME = "cdextractor_";
    private static final String DEFAULT_LAST_DIRECTORY = System.getProperty("user.home");

    private Properties properties;
    private File settingsFile;

    public SettingsManager() {
        properties = new Properties();
        settingsFile = new File(SETTINGS_FILE);
        loadSettings();
    }

    private void loadSettings() {
        if (settingsFile.exists()) {
            try (FileInputStream input = new FileInputStream(settingsFile)) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Error loading settings: " + e.getMessage());
                setDefaultSettings();
            }
        } else {
            setDefaultSettings();
        }
    }

    private void setDefaultSettings() {
        properties.setProperty("last.directory", DEFAULT_LAST_DIRECTORY);
        properties.setProperty("default.file.name", DEFAULT_FILE_NAME);
        properties.setProperty("current.index", "1");
        properties.setProperty("use.index", "true");
        saveSettings();
    }

    public void saveSettings() {
        try (FileOutputStream output = new FileOutputStream(settingsFile)) {
            properties.store(output, "CrystalData Extractor Settings");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    // Getters and Setters
    public String getLastDirectory() {
        return properties.getProperty("last.directory", DEFAULT_LAST_DIRECTORY);
    }

    public void setLastDirectory(String directory) {
        properties.setProperty("last.directory", directory);
        saveSettings();
    }

    public String getDefaultFileName() {
        return properties.getProperty("default.file.name", DEFAULT_FILE_NAME);
    }

    public void setDefaultFileName(String fileName) {
        properties.setProperty("default.file.name", fileName);
        saveSettings();
    }

    public int getCurrentIndex() {
        try {
            return Integer.parseInt(properties.getProperty("current.index", "1"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public void setCurrentIndex(int index) {
        properties.setProperty("current.index", String.valueOf(index));
        saveSettings();
    }

    public boolean isUseIndex() {
        return Boolean.parseBoolean(properties.getProperty("use.index", "true"));
    }

    public void setUseIndex(boolean useIndex) {
        properties.setProperty("use.index", String.valueOf(useIndex));
        saveSettings();
    }
}