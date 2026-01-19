import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ManagerOrdersController implements ActionListener {

    private ManagerOrdersView view;

    public ManagerOrdersController(ManagerOrdersView view) {
        this.view = view;
        this.view.setController(this);
        view.getBtnRefresh().addActionListener(this);
        view.getBtnMarkPaid().addActionListener(this);
    }

    public void loadOrders() {
        view.clearRows();
        List<Order> orders = Application.getInstance().getDataAdapter().loadAllOrders();
        for (Order o : orders) {
            Object[] row = new Object[4];
            row[0] = o.getOrderID();
            row[1] = o.getDate();
            row[2] = o.getTotalCost();
            row[3] = o.getPaymentStatus();
            view.addRow(row);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnRefresh()) {
            loadOrders();
        } else if (e.getSource() == view.getBtnMarkPaid()) {
            int row = view.getTblOrders().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Select an order first.");
                return;
            }
            int orderId = (Integer) view.getTblOrders().getValueAt(row, 0);
            Application.getInstance().getDataAdapter().updatePaymentStatus(orderId, "Paid");
            loadOrders();
        }
    }
}
