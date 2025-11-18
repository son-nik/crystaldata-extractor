import org.github.son_nik.crystaldata.view.MainFrame;
import org.github.son_nik.crystaldata.controller.MainController;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Запускаем в Event Dispatch Thread для Swing
        SwingUtilities.invokeLater(() -> {
            MainFrame view = new MainFrame();
            MainController controller = new MainController(view);
            view.setVisible(true);
        });
    }
}