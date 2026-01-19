import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController implements ActionListener {
    private LoginScreen loginScreen;

    public LoginController(LoginScreen loginScreen) {
        this.loginScreen = loginScreen;
        this.loginScreen.getBtnLogin().addActionListener(this);
        this.loginScreen.getBtnRegister().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginScreen.getBtnLogin()) {
            handleLogin();
        } else if (e.getSource() == loginScreen.getBtnRegister()) {
            handleRegister();
        }
    }

    private void handleLogin() {
        String username = loginScreen.getTxtUserName().getText().trim();
        String password = new String(((JPasswordField) loginScreen.getTxtPassword()).getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(loginScreen, "Username and password are required.");
            return;
        }

        User user = Application.getInstance().getDataAdapter().loadUser(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(loginScreen, "Invalid username or password.");
        } else {
            Application.getInstance().setCurrentUser(user);
            loginScreen.setVisible(false);
            Application.getInstance().getMainScreen().setVisible(true);
        }
    }

    private void handleRegister() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JCheckBox managerBox = new JCheckBox("Register as manager");

        // NEW: manager code field (hidden until checkbox clicked)
        JTextField managerCodeField = new JTextField();
        JLabel managerCodeLabel = new JLabel("Manager Code:");
        managerCodeLabel.setVisible(false);
        managerCodeField.setVisible(false);

        // Show manager code field when the checkbox is selected
        managerBox.addActionListener(e -> {
            boolean selected = managerBox.isSelected();
            managerCodeLabel.setVisible(selected);
            managerCodeField.setVisible(selected);
        });

        JPanel panel = new JPanel();
        panel.setLayout(new SpringLayout());
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(new JLabel("Full name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        // Role checkbox
        panel.add(new JLabel("Role:"));
        panel.add(managerBox);

        // add manager code (hidden unless manager selected)
        panel.add(managerCodeLabel);
        panel.add(managerCodeField);

        SpringUtilities.makeCompactGrid(panel,
                6, 2,   // now 6 rows
                6, 6,
                6, 6);

        int result = JOptionPane.showConfirmDialog(loginScreen, panel,
                "Register New User", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() ||
                fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(loginScreen, "All fields are required.");
            return;
        }

        boolean wantsManager = managerBox.isSelected();

        // If registering as manager, verify the code
        if (wantsManager) {
            String code = managerCodeField.getText().trim();
            if (!code.equals("11111")) {
                JOptionPane.showMessageDialog(loginScreen,
                        "Incorrect manager code. Registration denied.");
                return;
            }
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setManager(wantsManager);

        boolean ok = Application.getInstance().getDataAdapter().registerUser(newUser);
        if (ok) {
            JOptionPane.showMessageDialog(loginScreen, "Registration successful. You can log in now.");
        } else {
            JOptionPane.showMessageDialog(loginScreen, "Registration failed (username may already exist).");
        }
    }

}
