import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ManagerOrdersView extends JFrame {

    private DefaultTableModel model = new DefaultTableModel();
    private JTable tblOrders = new JTable(model);
    private JButton btnRefresh = new JButton("Refresh");
    private JButton btnMarkPaid = new JButton("Mark as Paid");

    public ManagerOrdersView() {
        this.setTitle("All Orders");
        this.setSize(600, 400);
        this.setLayout(new BorderLayout());

        model.addColumn("Order ID");
        model.addColumn("Date");
        model.addColumn("Total");
        model.addColumn("Status");

        JScrollPane scroll = new JScrollPane(tblOrders);
        this.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.add(btnRefresh);
        bottom.add(btnMarkPaid);
        this.add(bottom, BorderLayout.SOUTH);
    }

    public JTable getTblOrders() { return tblOrders; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnMarkPaid() { return btnMarkPaid; }

    public void clearRows() {
        model.setRowCount(0);
    }

    public void addRow(Object[] row) {
        model.addRow(row);
    }

    public void refreshTable() {
        // Controller will actually fill this, but we need a public hook for MainScreen.
        ManagerOrdersController controller =
                Application.getInstance().getManagerOrdersView().getController();
        if (controller != null) {
            controller.loadOrders();
        }
    }

    // helper for controller injection
    private ManagerOrdersController controller;
    public void setController(ManagerOrdersController c) {
        this.controller = c;
    }
    public ManagerOrdersController getController() {
        return controller;
    }
}
