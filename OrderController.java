import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class OrderController implements ActionListener {
    private OrderView view;
    private Order order;

    public OrderController(OrderView view) {
        this.view = view;
        view.getBtnAdd().addActionListener(this);
        view.getBtnPay().addActionListener(this);
        order = new Order();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnAdd()) {
            addItem();
        } else if (e.getSource() == view.getBtnPay()) {
            pay();
        }
    }

    private void addItem() {
        User current = Application.getInstance().getCurrentUser();
        if (current == null) {
            JOptionPane.showMessageDialog(view, "You must be logged in as a customer to order.");
            return;
        }
        if (current.isManager()) {
            JOptionPane.showMessageDialog(view, "Managers are not allowed to place orders.");
            return;
        }

        String keyword = JOptionPane.showInputDialog(view,
                "Enter title or author keyword:", "Search Books",
                JOptionPane.QUESTION_MESSAGE);
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        List<Product> results = Application.getInstance().getDataAdapter().searchBooks(keyword.trim());
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(view, "No books found.");
            return;
        }

        Product chosen;
        if (results.size() == 1) {
            chosen = results.get(0);
        } else {
            // Let the user choose from a list
            String[] options = new String[results.size()];
            for (int i = 0; i < results.size(); i++) {
                Product p = results.get(i);
                options[i] = p.getProductID() + " - " + p.getTitle() + " by " + p.getAuthor() +
                        " ($" + p.getPriceBuy() + " buy / $" + p.getPriceRent() + " rent)";
            }
            String selected = (String) JOptionPane.showInputDialog(view,
                    "Select a book:",
                    "Search Results",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (selected == null) {
                return;
            }
            int idx = 0;
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(selected)) {
                    idx = i;
                    break;
                }
            }
            chosen = results.get(idx);
        }

        String[] typeOptions = {"BUY", "RENT"};
        String type = (String) JOptionPane.showInputDialog(view,
                "Buy or rent?",
                "Type",
                JOptionPane.PLAIN_MESSAGE,
                null,
                typeOptions,
                typeOptions[0]);
        if (type == null) {
            return;
        }

        String qtyStr = JOptionPane.showInputDialog(view,
                "Quantity (available: " + chosen.getQuantity() + "):",
                "1");
        if (qtyStr == null || qtyStr.trim().isEmpty()) {
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Quantity must be an integer.");
            return;
        }
        if (qty <= 0) {
            JOptionPane.showMessageDialog(view, "Quantity must be > 0.");
            return;
        }
        if (qty > chosen.getQuantity()) {
            JOptionPane.showMessageDialog(view, "Not enough stock.");
            return;
        }

        double unitPrice = type.equals("BUY") ? chosen.getPriceBuy() : chosen.getPriceRent();

        OrderLine line = new OrderLine();
        line.setProductID(chosen.getProductID());
        line.setType(type);
        line.setUnitPrice(unitPrice);
        line.setQuantity(qty);

        order.addLine(line);

        Object[] row = new Object[6];
        row[0] = chosen.getProductID();
        row[1] = chosen.getTitle();
        row[2] = type;
        row[3] = unitPrice;
        row[4] = qty;
        row[5] = line.getCost();

        view.addRow(row);
        view.getLabTotal().setText("Total: $" + String.format("%.2f", order.getTotalCost()));
        view.invalidate();
    }

    private void pay() {
        if (order.getLines().isEmpty()) {
            JOptionPane.showMessageDialog(view, "No items in the order.");
            return;
        }

        User current = Application.getInstance().getCurrentUser();
        if (current == null) {
            JOptionPane.showMessageDialog(view, "You must be logged in to place an order.");
            return;
        }
        
        if (current.isManager()) {
            JOptionPane.showMessageDialog(view, "Managers are not allowed to place orders.");
            return;
        }

        int orderId = Application.getInstance().getDataAdapter().saveOrder(order, current);
        if (orderId <= 0) {
            JOptionPane.showMessageDialog(view, "Failed to save order.");
            return;
        }
        order.setOrderID(orderId);

        String bill = buildBill(order, current);
        JOptionPane.showMessageDialog(view, bill, "Order Placed", JOptionPane.INFORMATION_MESSAGE);

        // Email stub – here you’d call real email sending code
        EmailUtil.sendBillStub(current.getEmail(), bill);

        // reset for next order
        order = new Order();
        view.clearTable();
        view.getLabTotal().setText("Total: $0.00");
    }

    private String buildBill(Order order, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(order.getOrderID()).append("\n");
        sb.append("Customer: ").append(user.getFullName()).append(" (").append(user.getEmail()).append(")\n\n");
        sb.append(String.format("%-8s %-25s %-6s %-8s %-8s%n",
                "BookID", "Title", "Type", "Qty", "Subtotal"));

        for (OrderLine line : order.getLines()) {
            sb.append(String.format("%-8d %-25s %-6s %-8d $%-8.2f%n",
                    line.getProductID(),
                    "", // title omitted here; we showed titles in the GUI; could look up again if needed
                    line.getType(),
                    line.getQuantity(),
                    line.getCost()));
        }

        sb.append("\nTotal due: $").append(String.format("%.2f", order.getTotalCost())).append("\n");
        sb.append("Payment status: Pending\n");
        return sb.toString();
    }
}
