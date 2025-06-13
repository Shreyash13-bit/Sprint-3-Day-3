package nisum.ascend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.*;
import javax.validation.constraints.*;
import javax.validation.*;
import java.util.*;
import java.util.stream.*;
import java.time.LocalDateTime;
@SpringBootApplication
@RestController
@RequestMapping("/products")
public class Q5 {
    private static final Map<Integer, Product> productDB = new HashMap<>();
    private static int idCounter = 1;
    public static void main(String[] args) {
        SpringApplication.run(ProductApi.class, args);
    }
    static class Product {
        private int id;
        @NotNull @Size(min = 2, message = "Name too short")
        private String name;
        @Size(max = 200, message = "Description too long")
        private String description;
        @Min(value = 0, message = "Price must be positive")
        private double price;
        @Min(value = 0, message = "Stock must be positive")
        private int stockQuantity;
        @NotNull(message = "Category is required")
        private String category;
        public Product() {}
        public Product(int id, String name, String desc, double price, int stock, String category) {
            this.id = id; this.name = name; this.description = desc;
            this.price = price; this.stockQuantity = stock; this.category = category;
        }
        public int getId() { return id; } public void setId(int id) { this.id = id; }
        public String getName() { return name; } public void setName(String name) { this.name = name; }
        public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
        public double getPrice() { return price; } public void setPrice(double price) { this.price = price; }
        public int getStockQuantity() { return stockQuantity; } public void setStockQuantity(int stock) { this.stockQuantity = stock; }
        public String getCategory() { return category; } public void setCategory(String category) { this.category = category; }
    }
    @GetMapping
    public List<Product> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort
    ) {
        Stream<Product> stream = productDB.values().stream();
        if (category != null) stream = stream.filter(p -> p.getCategory().equalsIgnoreCase(category));
        if (minPrice != null) stream = stream.filter(p -> p.getPrice() >= minPrice);
        if (maxPrice != null) stream = stream.filter(p -> p.getPrice() <= maxPrice);
        if (sort != null) {
            String[] parts = sort.split(",");
            Comparator<Product> comp = switch (parts[0]) {
                case "price" -> Comparator.comparing(Product::getPrice);
                case "name" -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
                default -> Comparator.comparing(Product::getId);
            };
            if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) comp = comp.reversed();
            stream = stream.sorted(comp);
        }
        return stream
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }
    @GetMapping("/{id}")
    public Product getById(@PathVariable int id) {
        Product p = productDB.get(id);
        if (p == null) throw new RuntimeException("Product with ID " + id + " not found");
        return p;
    } 
    @PostMapping
    public ResponseEntity<String> addProduct(@Valid @RequestBody Product product) {
        product.setId(idCounter++);
        productDB.put(product.getId(), product);
        return ResponseEntity.ok("Product added.");
    } 
    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable int id, @Valid @RequestBody Product product) {
        if (!productDB.containsKey(id)) return ResponseEntity.status(404).body("Product not found.");
        product.setId(id);
        productDB.put(id, product);
        return ResponseEntity.ok("Product updated.");
    } 
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        if (productDB.remove(id) == null) return ResponseEntity.status(404).body("Product not found.");
        return ResponseEntity.ok("Product deleted.");
    } 
    @ControllerAdvice
    public static class ErrorHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("timestamp", LocalDateTime.now());
            err.put("status", 400);
            err.put("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.toList()));
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        }
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, WebRequest req) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("timestamp", LocalDateTime.now());
            err.put("status", 404);
            err.put("message", ex.getMessage());
            err.put("path", req.getDescription(false).replace("uri=", ""));
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
    }
}
