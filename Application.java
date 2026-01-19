import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Application {

    private static Application instance;   // Singleton pattern

    public static Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }
        return instance;
    }

    private Connection connection;
    private DataAdapter dataAdapter;

    private User currentUser;

    private LoginScreen loginScreen;
    private MainScreen mainScreen;
    private ProductView productView;
    private OrderView orderView;
    private ManagerOrdersView managerOrdersView;

    private ProductController productController;
    private OrderController orderController;
    private LoginController loginController;
    private ManagerOrdersController managerOrdersController;

    private Application() {
        connectToDatabase();

        loginScreen = new LoginScreen();
        mainScreen = new MainScreen();
        productView = new ProductView();
        orderView = new OrderView();
        managerOrdersView = new ManagerOrdersView();

        productController = new ProductController(productView);
        orderController = new OrderController(orderView);
        loginController = new LoginController(loginScreen);
        managerOrdersController = new ManagerOrdersController(managerOrdersView);
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // TODO: change user/password to match your MySQL setup
            String url = "jdbc:mysql://localhost:3306/bookstore?serverTimezone=UTC";
            String user = "root";
            String password = "MySQL";
            connection = DriverManager.getConnection(url, user, password);
            dataAdapter = new DataAdapter(connection);
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public DataAdapter getDataAdapter() {
        return dataAdapter;
    }

    public LoginScreen getLoginScreen() {
        return loginScreen;
    }

    public MainScreen getMainScreen() {
        return mainScreen;
    }

    public ProductView getProductView() {
        return productView;
    }

    public OrderView getOrderView() {
        return orderView;
    }

    public ManagerOrdersView getManagerOrdersView() {
        return managerOrdersView;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            mainScreen.configureForUser(user);
        }
    }

    public void logout() {
        currentUser = null;
        mainScreen.setVisible(false);
        loginScreen.setVisible(true);
    }

    public static void main(String[] args) {
        Application.getInstance().getLoginScreen().setVisible(true);
    }
}
