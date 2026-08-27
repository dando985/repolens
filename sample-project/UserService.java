import java.util.HashMap;
import java.util.Map;

public class UserService {

    private final Map<String, String> users = new HashMap<>();

    public void registerUser(String email, String password) {
        users.put(email, password);
    }

    public boolean authenticateUser(String email, String password) {
        String storedPassword = users.get(email);

        return storedPassword != null && storedPassword.equals(password);
    }

    public boolean userExists(String email) {
        return users.containsKey(email);
    }
}