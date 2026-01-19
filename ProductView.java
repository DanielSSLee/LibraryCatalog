import javax.swing.*;

public class ProductView extends JFrame {
    private JTextField txtProductID = new JTextField(10);
    private JTextField txtTitle = new JTextField(30);
    private JTextField txtAuthor = new JTextField(30);
    private JTextField txtPriceBuy = new JTextField(10);
    private JTextField txtPriceRent = new JTextField(10);
    private JTextField txtQuantity = new JTextField(10);

    private JButton btnLoad = new JButton("Load Book");
    private JButton btnSave = new JButton("Save Book");

    public ProductView() {
        this.setTitle("Manage Books");
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
        this.setSize(500, 250);

        JPanel main = new JPanel();
        main.setLayout(new SpringLayout());

        main.add(new JLabel("Book ID (0 for new):"));
        main.add(txtProductID);
        main.add(new JLabel("Title:"));
        main.add(txtTitle);
        main.add(new JLabel("Author:"));
        main.add(txtAuthor);
        main.add(new JLabel("Buy Price:"));
        main.add(txtPriceBuy);
        main.add(new JLabel("Rent Price:"));
        main.add(txtPriceRent);
        main.add(new JLabel("Quantity:"));
        main.add(txtQuantity);

        SpringUtilities.makeCompactGrid(main,
                6, 2,
                6, 6,
                6, 6);

        JPanel buttons = new JPanel();
        buttons.add(btnLoad);
        buttons.add(btnSave);

        this.getContentPane().add(main);
        this.getContentPane().add(buttons);
    }

    public JTextField getTxtProductID() { return txtProductID; }
    public JTextField getTxtTitle() { return txtTitle; }
    public JTextField getTxtAuthor() { return txtAuthor; }
    public JTextField getTxtPriceBuy() { return txtPriceBuy; }
    public JTextField getTxtPriceRent() { return txtPriceRent; }
    public JTextField getTxtQuantity() { return txtQuantity; }

    public JButton getBtnLoad() { return btnLoad; }
    public JButton getBtnSave() { return btnSave; }
}
