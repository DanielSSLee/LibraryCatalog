import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataAdapter {
    private Connection connection;

    public DataAdapter(Connection connection) {
        this.connection = connection;
    }

    // ---------- Password hashing (NFR2.1) ----------
    public static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ---------- Books (Products) ----------

    public Product loadProduct(int id) {
        String sql = "SELECT * FROM Books WHERE BookID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Product p = new Product();
                p.setProductID(rs.getInt("BookID"));
                p.setTitle(rs.getString("Title"));
                p.setAuthor(rs.getString("Author"));
                p.setPriceBuy(rs.getDouble("PriceBuy"));
                p.setPriceRent(rs.getDouble("PriceRent"));
                p.setQuantity(rs.getInt("Quantity"));
                return p;
            }
        } catch (SQLException e) {
            System.out.println("Error loading book: " + e.getMessage());
        }
        return null;
    }

    public void saveProduct(Product p) {
        if (p.getProductID() == 0) {
            // Insert
            String sql = "INSERT INTO Books (Title, Author, PriceBuy, PriceRent, Quantity) VALUES (?,?,?,?,?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, p.getTitle());
                stmt.setString(2, p.getAuthor());
                stmt.setDouble(3, p.getPriceBuy());
                stmt.setDouble(4, p.getPriceRent());
                stmt.setInt(5, p.getQuantity());
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    p.setProductID(keys.getInt(1));
                }
            } catch (SQLException e) {
                System.out.println("Error inserting book: " + e.getMessage());
            }
        } else {
            // Update
            String sql = "UPDATE Books SET Title=?, Author=?, PriceBuy=?, PriceRent=?, Quantity=? WHERE BookID=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, p.getTitle());
                stmt.setString(2, p.getAuthor());
                stmt.setDouble(3, p.getPriceBuy());
                stmt.setDouble(4, p.getPriceRent());
                stmt.setInt(5, p.getQuantity());
                stmt.setInt(6, p.getProductID());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error updating book: " + e.getMessage());
            }
        }
    }

    public List<Product> searchBooks(String keyword) {
        List<Product> results = new ArrayList<>();
        String sql = "SELECT * FROM Books WHERE Title LIKE ? OR Author LIKE ? ORDER BY Title";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            stmt.setString(1, k);
            stmt.setString(2, k);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setProductID(rs.getInt("BookID"));
                p.setTitle(rs.getString("Title"));
                p.setAuthor(rs.getString("Author"));
                p.setPriceBuy(rs.getDouble("PriceBuy"));
                p.setPriceRent(rs.getDouble("PriceRent"));
                p.setQuantity(rs.getInt("Quantity"));
                results.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Error searching books: " + e.getMessage());
        }
        return results;
    }

    // ---------- Users ----------

    public boolean registerUser(User user) {
        String sql = "INSERT INTO Users (UserName, PasswordHash, FullName, Email, IsManager) VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashPassword(user.getPassword()));
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setBoolean(5, user.isManager());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public User loadUser(String username, String passwordPlain) {
        String sql = "SELECT * FROM Users WHERE UserName = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            String storedHash = rs.getString("PasswordHash");
            String incomingHash = hashPassword(passwordPlain);
            if (!storedHash.equals(incomingHash)) {
                return null;
            }
            User u = new User();
            u.setUserID(rs.getInt("UserID"));
            u.setUsername(rs.getString("UserName"));
            u.setFullName(rs.getString("FullName"));
            u.setEmail(rs.getString("Email"));
            u.setManager(rs.getBoolean("IsManager"));
            return u;
        } catch (SQLException e) {
            System.out.println("Error loading user: " + e.getMessage());
            return null;
        }
    }

    // ---------- Orders ----------

    public int saveOrder(Order order, User buyer) {
        String insertOrder = "INSERT INTO Orders (UserID, OrderDate, TotalAmount, PaymentStatus) VALUES (?,?,?,?)";
        String insertLine = "INSERT INTO OrderLines (OrderID, BookID, Quantity, Type, UnitPrice, LineTotal) VALUES (?,?,?,?,?,?)";
        String updateQty = "UPDATE Books SET Quantity = Quantity - ? WHERE BookID = ?";

        try {
            connection.setAutoCommit(false);

            // Insert order
            int orderID;
            try (PreparedStatement stmt = connection.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, buyer.getUserID());
                LocalDateTime now = LocalDateTime.now();
                stmt.setTimestamp(2, Timestamp.valueOf(now));
                stmt.setDouble(3, order.getTotalCost());
                stmt.setString(4, "Pending");
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return -1;
                }
                orderID = keys.getInt(1);
            }

            // Insert lines
            try (PreparedStatement lineStmt = connection.prepareStatement(insertLine);
                 PreparedStatement qtyStmt = connection.prepareStatement(updateQty)) {

                for (OrderLine line : order.getLines()) {
                    lineStmt.setInt(1, orderID);
                    lineStmt.setInt(2, line.getProductID());
                    lineStmt.setInt(3, line.getQuantity());
                    lineStmt.setString(4, line.getType());
                    lineStmt.setDouble(5, line.getUnitPrice());
                    lineStmt.setDouble(6, line.getCost());
                    lineStmt.addBatch();

                    // Decrease quantity for both buy & rent (simple model)
                    qtyStmt.setInt(1, line.getQuantity());
                    qtyStmt.setInt(2, line.getProductID());
                    qtyStmt.addBatch();
                }

                lineStmt.executeBatch();
                qtyStmt.executeBatch();
            }

            connection.commit();
            connection.setAutoCommit(true);
            return orderID;

        } catch (SQLException e) {
            System.out.println("Error saving order: " + e.getMessage());
            try { connection.rollback(); } catch (SQLException ignored) {}
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
            return -1;
        }
    }

    public List<Order> loadAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.OrderID, o.OrderDate, o.TotalAmount, o.PaymentStatus, " +
                "u.FullName, u.Email " +
                "FROM Orders o JOIN Users u ON o.UserID = u.UserID ORDER BY o.OrderDate DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            while (rs.next()) {
                Order o = new Order();
                o.setOrderID(rs.getInt("OrderID"));
                o.setTotalCost(rs.getDouble("TotalAmount"));
                o.setPaymentStatus(rs.getString("PaymentStatus"));
                Timestamp ts = rs.getTimestamp("OrderDate");
                if (ts != null) {
                    o.setDate(ts.toLocalDateTime().format(fmt));
                }
                // We'll pass customer name/email back via OrderView/ManagerOrdersView using ResultSet directly if needed
                orders.add(o);
            }
        } catch (SQLException e) {
            System.out.println("Error loading orders: " + e.getMessage());
        }
        return orders;
    }

    public void updatePaymentStatus(int orderId, String status) {
        String sql = "UPDATE Orders SET PaymentStatus = ? WHERE OrderID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating payment status: " + e.getMessage());
        }
    }
}
