import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {

    private final Map<String, List<String>> ordersByCustomer = new HashMap<>();

    public void createOrder(String customerId, String productName) {
        ordersByCustomer.computeIfAbsent(customerId, key -> new ArrayList<>()).add(productName);
    }

    public List<String> findOrdersByCustomer(String customerId) {
        return new ArrayList<>(ordersByCustomer.getOrDefault(customerId, List.of()));
    }

    public double calculateOrderTotal(double price, int quantity, double taxRate) {
        double subtotal = price * quantity;
        return subtotal + subtotal * taxRate;
    }
}