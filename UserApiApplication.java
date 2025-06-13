package nisum.ascend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/users")
public class UserApiApplication {
    private List<User> users = Arrays.asList(
        new User(1, "Alice"),
        new User(2, "Bob")
    );
    public static void main(String[] args) {
        SpringApplication.run(UserApiApplication.class, args);
    }
    static class User {
        private int id;
        private String name;
        public User() {}
        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String msg) {
            super(msg);
        }
    }
    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        return users.stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }
    @ControllerAdvice
    static class GlobalExceptionHandler {
        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<Object> handleUserNotFound(UserNotFoundException ex, WebRequest req) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("timestamp", LocalDateTime.now());
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("error", "Not Found");
            error.put("message", ex.getMessage());
            error.put("path", req.getDescription(false).replace("uri=", ""));
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }
}
