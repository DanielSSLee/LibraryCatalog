import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProductController implements ActionListener {
    private ProductView productView;

    public ProductController(ProductView productView) {
        this.productView = productView;

        productView.getBtnLoad().addActionListener(this);
        productView.getBtnSave().addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == productView.getBtnLoad()) {
            loadBook();
        } else if (e.getSource() == productView.getBtnSave()) {
            saveBook();
        }
    }

    private void loadBook() {
        int id;
        try {
            id = Integer.parseInt(productView.getTxtProductID().getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(productView, "Invalid Book ID.");
            return;
        }

        Product product = Application.getInstance().getDataAdapter().loadProduct(id);

        if (product == null) {
            JOptionPane.showMessageDialog(productView, "Book not found.");
            return;
        }

        productView.getTxtTitle().setText(product.getTitle());
        productView.getTxtAuthor().setText(product.getAuthor());
        productView.getTxtPriceBuy().setText(String.valueOf(product.getPriceBuy()));
        productView.getTxtPriceRent().setText(String.valueOf(product.getPriceRent()));
        productView.getTxtQuantity().setText(String.valueOf(product.getQuantity()));
    }

    private void saveBook() {
        Product p = new Product();

        try {
            String idText = productView.getTxtProductID().getText().trim();
            int id = idText.isEmpty() ? 0 : Integer.parseInt(idText);
            p.setProductID(id);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(productView, "Book ID must be a number.");
            return;
        }

        p.setTitle(productView.getTxtTitle().getText().trim());
        p.setAuthor(productView.getTxtAuthor().getText().trim());

        if (p.getTitle().isEmpty() || p.getAuthor().isEmpty()) {
            JOptionPane.showMessageDialog(productView, "Title and author are required.");
            return;
        }

        try {
            p.setPriceBuy(Double.parseDouble(productView.getTxtPriceBuy().getText().trim()));
            p.setPriceRent(Double.parseDouble(productView.getTxtPriceRent().getText().trim()));
            p.setQuantity(Integer.parseInt(productView.getTxtQuantity().getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(productView, "Price and quantity must be numbers.");
            return;
        }

        Application.getInstance().getDataAdapter().saveProduct(p);
        JOptionPane.showMessageDialog(productView, "Book saved.");
    }
}
