import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Show login dialog first
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true);

                // Only proceed if authentication was successful
                if (loginDialog.isAuthenticated()) {
                    EventManagerGUI gui = new EventManagerGUI();
                    gui.setVisible(true);
                } else {
                    System.exit(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}