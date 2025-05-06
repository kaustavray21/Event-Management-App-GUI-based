import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;
import java.io.*;

public class LoginDialog extends JDialog {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private boolean authenticated = false;

    // Use your existing color scheme
    private final Color PRIMARY_COLOR = new Color(34, 139, 34); // Forest green
    private final Color BACKGROUND_COLOR = new Color(240, 255, 240); // Light green background
    private final Color TEXT_COLOR = Color.BLACK;

    public LoginDialog(JFrame parent) {
        super(parent, "Admin Login", true);
        setSize(400, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        panel.add(usernameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        panel.add(passwordField, gbc);

        // Login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JButton loginButton = createStyledButton("Login");
        panel.add(loginButton, gbc);

        loginButton.addActionListener(e -> authenticate());

        add(panel, BorderLayout.CENTER);

        // Load credentials from properties file
        loadCredentials();
    }

    private void styleTextField(JComponent field) {
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        return button;
    }

    private void authenticate() {
        String username = usernameField.getText();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check against stored credentials
        Properties credentials = loadCredentials();
        String storedUsername = credentials.getProperty("admin.username");
        String storedHash = credentials.getProperty("admin.password.hash");
        String salt = credentials.getProperty("admin.salt");

        if (username.equals(storedUsername)) {
            String inputHash = hashPassword(new String(password), salt);
            if (inputHash.equals(storedHash)) {
                authenticated = true;
                dispose();
                return;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Invalid username or password",
                "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        passwordField.setText("");
    }

    private Properties loadCredentials() {
        Properties props = new Properties();
        File configFile = new File("admin.config");

        if (!configFile.exists()) {
            // Create default credentials if file doesn't exist
            props.setProperty("admin.username", "admin");
            props.setProperty("admin.salt", generateRandomSalt());
            props.setProperty("admin.password.hash",
                    hashPassword("admin123", props.getProperty("admin.salt")));

            try (OutputStream out = new FileOutputStream(configFile)) {
                props.store(out, "Admin Credentials");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error creating credentials file",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error reading credentials file",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        return props;
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = salt + password;
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    private String generateRandomSalt() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}