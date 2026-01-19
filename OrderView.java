import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderView extends JFrame {

    private JButton btnAdd = new JButton("Add Book");
    private JButton btnPay = new JButton("Place Order");

    private DefaultTableModel items = new DefaultTableModel();
    private JTable tblItems = new JTable(items);
    private JLabel labTotal = new JLabel("Total: $0.00");

    public OrderView() {
        this.setTitle("New Order");
        this.setSize(700, 400);
        this.setLayout(new BorderLayout());

        items.addColumn("Book ID");
        items.addColumn("Title");
        items.addColumn("Type");
        items.addColumn("Unit Price");
        items.addColumn("Quantity");
        items.addColumn("Line Total");

        JScrollPane scrollPane = new JScrollPane(tblItems);
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel left = new JPanel();
        left.add(btnAdd);
        left.add(btnPay);
        bottom.add(left, BorderLayout.WEST);
        bottom.add(labTotal, BorderLayout.EAST);

        this.add(bottom, BorderLayout.SOUTH);
    }

    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnPay() { return btnPay; }
    public JTable getTblItems() { return tblItems; }
    public JLabel getLabTotal() { return labTotal; }

    public void addRow(Object[] row) {
        items.addRow(row);
    }

    public void clearTable() {
        DefaultTableModel m = (DefaultTableModel) tblItems.getModel();
        m.setRowCount(0);
    }
}
