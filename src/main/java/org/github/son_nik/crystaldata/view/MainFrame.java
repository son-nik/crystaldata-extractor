package org.github.son_nik.crystaldata.view;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenuItem menuFileOpen;
    private JMenuItem menuFileSave;
    private JMenu menuHelp;
    private JMenuItem menuHelpAbout;
    private JMenuItem menuHelpUpdate;
    private JMenuItem menuFileSettings;
    private JFileChooser fileChooser;
    private JTextArea resultTextArea;
    private JLabel statusLabel;

    public MainFrame() {
        fileChooser = new JFileChooser();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("CrystalData Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
        setIconImage(icon.getImage());

        createMenuBar();
        createMainPanel();
        createStatusBar();

    }

    private void createMenuBar() {
        menuBar = new JMenuBar();

        menuFile = new JMenu("File");

        menuFileOpen = new JMenuItem("open file");
        menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));

        menuFileSave = new JMenuItem("save file");
        menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));

        menuFileSettings = new JMenuItem("Settings");


        menuHelp = new JMenu("Help");
        menuHelp.setMnemonic(KeyEvent.VK_H);

        menuHelpAbout = new JMenuItem("About");
        menuHelpAbout.addActionListener(e -> showAboutDialog());
        menuHelpUpdate = new JMenuItem("Update");
        menuHelpUpdate.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Coming soon"));

        menuFile.add(menuFileOpen);
        menuFile.add(menuFileSave);
        menuFile.addSeparator();
        menuFile.add(menuFileSettings);

        menuHelp.add(menuHelpAbout);
        menuHelp.add(menuHelpUpdate);

        menuBar.add(menuFile);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);
    }

    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultTextArea);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void createStatusBar() {
        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void showAboutDialog() {
        String aboutMessage = "CrystalData Extractor\n\n" +
                "Developer: Sonin-Nikita\n" +
                "Email: Nik.Son.31@yandex.ru,\n" +
                "niksonvi39@gmail.com\n" +
                "GitHub: github.com/son-nik\n\n" +
                "Application for parsing LIS files from X-ray crystallography";
        JOptionPane.showMessageDialog(this, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    public void setOpenButtonListener(ActionListener listener) {
        menuFileOpen.addActionListener(listener);
    }

    public void setSaveButtonListener(ActionListener listener) {
        menuFileSave.addActionListener(listener);
    }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public String getResultText() {
        return resultTextArea.getText();
    }

    public void setResultText(String formattedResults) {
        resultTextArea.setText(formattedResults);
    }

    public void setStatusText(String status) {
        statusLabel.setText(" " + status);
    }

    public void setSettingsButtonListener(ActionListener listener) {
        menuFileSettings.addActionListener(listener);
    }
}
