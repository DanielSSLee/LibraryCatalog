import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainScreen extends JFrame {

    private JButton btnBuy = new JButton("Search & Order Books");
    private JButton btnSell = new JButton("Manage Books");
    private JButton btnViewOrders = new JButton("View Orders (Manager)");
    private JButton btnLogout = new JButton("Logout");

    public MainScreen() {
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 250);

        btnSell.setPreferredSize(new Dimension(160, 40));
        btnBuy.setPreferredSize(new Dimension(160, 40));

        this.add(Box.createVerticalStrut(10));
        this.add(center(btnBuy));
        this.add(center(btnSell));
        this.add(center(btnViewOrders));
        this.add(center(btnLogout));

        btnBuy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.getInstance().getOrderView().setVisible(true);
            }
        });

        btnSell.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.getInstance().getProductView().setVisible(true);
            }
        });

        btnViewOrders.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.getInstance().getManagerOrdersView().refreshTable();
                Application.getInstance().getManagerOrdersView().setVisible(true);
            }
        });

        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.getInstance().logout();
            }
        });
    }

    private JPanel center(JComponent comp) {
        JPanel p = new JPanel();
        p.add(comp);
        return p;
    }

    public void configureForUser(User user) {
        this.setTitle("Bookstore - Logged in as " +
            user.getFullName() + (user.isManager() ? " (Manager)" : " (Customer)"));

        if (user.isManager()) {
            // MANAGER: can manage books & view orders, but CANNOT order books
            btnBuy.setVisible(false);
            btnSell.setVisible(true);
            btnViewOrders.setVisible(true);
        } else {
            // CUSTOMER: can order books, but CANNOT manage books or view all orders
            btnBuy.setVisible(true);
            btnSell.setVisible(false);
            btnViewOrders.setVisible(false);
        }
    }

}
