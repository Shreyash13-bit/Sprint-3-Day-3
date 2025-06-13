package nisum.ascend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.*;
import javax.validation.*;
import javax.validation.constraints.*;
import java.util.*;
import java.time.LocalDateTime;
@SpringBootApplication
@RestController
@RequestMapping("/users")
public class Q3 {
    private List<User> users = new ArrayList<>();
    public static void main(String[] args) {
        SpringApplication.run(UserValidationApi.class, args);
    }
    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody User user) {
        users.add(user);
        return ResponseEntity.ok("User added.");
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable int id, @Valid @RequestBody User user) {
        if (id < 0 || id >= users.size()) return ResponseEntity.status(404).body("User not found");
        users.set(id, user);
        return ResponseEntity.ok("User updated.");
    }
    public static class User {
        @NotNull(message = "Name is required")
        @Size(min = 2, message = "Name must be at least 2 characters")
        private String name;
        @NotNull(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
        @Min(value = 0, message = "Age cannot be negative")
        @CustomAgeCheck
        private int age;
        public User() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
    @Constraint(validatedBy = AgeValidator.class)
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomAgeCheck {
        String message() default "Age must be 18 or older";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
    public static class AgeValidator implements ConstraintValidator<CustomAgeCheck, Integer> {
        public void initialize(CustomAgeCheck age) {}
        public boolean isValid(Integer age, ConstraintValidatorContext cxt) {
            return age != null && age >= 18;
        }
    }
    @ControllerAdvice
    public static class ValidationErrorHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest req) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.BAD_REQUEST.value());
            List<String> errors = new ArrayList<>();
            for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                errors.add(error.getField() + ": " + error.getDefaultMessage());
            }
            body.put("errors", errors);
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
    }
}
