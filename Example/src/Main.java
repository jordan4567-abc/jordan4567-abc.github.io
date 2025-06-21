import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {  // 修正方法名大小写
            DataManager.loadInitialData();
            new GUI().initGUI();
        });
    }
}