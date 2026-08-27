public class NotificationService {

    public String createOrderConfirmation(String customerName, String orderId) {
        return "Hello " + customerName + ", your order " + orderId + " was completed.";
    }

    public String createPasswordResetMessage(String customerName, String resetLink) {
        return "Hello " + customerName + ", reset your password here: " + resetLink;
    }
}