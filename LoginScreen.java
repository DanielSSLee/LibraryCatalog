import javax.swing.*;

public class LoginScreen extends JFrame {
    private JTextField txtUserName = new JTextField(15);
    private JPasswordField txtPassword = new JPasswordField(15);
    private JButton btnLogin = new JButton("Login");
    private JButton btnRegister = new JButton("Register");

    public JButton getBtnLogin() {
        return btnLogin;
    }

    public JButton getBtnRegister() {
        return btnRegister;
    }

    public JTextField getTxtPassword() {
        return txtPassword;
    }

    public JTextField getTxtUserName() {
        return txtUserName;
    }

    public LoginScreen() {
        this.setTitle("Bookstore Login");
        this.setSize(350, 150);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        JPanel main = new JPanel();
        main.setLayout(new SpringLayout());

        main.add(new JLabel("Username:"));
        main.add(txtUserName);
        main.add(new JLabel("Password:"));
        main.add(txtPassword);

        SpringUtilities.makeCompactGrid(main,
                2, 2, //rows, cols
                6, 6,
                6, 6);

        JPanel buttons = new JPanel();
        buttons.add(btnLogin);
        buttons.add(btnRegister);

        this.getContentPane().add(main);
        this.getContentPane().add(buttons);
    }
}
