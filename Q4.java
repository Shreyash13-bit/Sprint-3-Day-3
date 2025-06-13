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
@RequestMapping("/books")
public class Q4 {
    private static final Map<Integer, Book> bookDB = new HashMap<>();
    private static int idCounter = 1;
    public static void main(String[] args) {
        SpringApplication.run(LibraryApi.class, args);
    }
    static class Book {
        private int id;
        @NotNull(message = "Title is required")
        @Size(min = 2, message = "Title must be at least 2 characters")
        private String title;
        @NotNull(message = "Author is required")
        private String author;
        @Min(value = 1000, message = "Published year must be valid")
        private int publishedYear;
        public Book() {}
        public Book(int id, String title, String author, int year) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.publishedYear = year;
        }
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public int getPublishedYear() { return publishedYear; }
        public void setPublishedYear(int publishedYear) { this.publishedYear = publishedYear; }
    }
    @GetMapping
    public List<Book> getAllBooks(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer publishedYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Stream<Book> stream = bookDB.values().stream();
        if (author != null) stream = stream.filter(b -> b.getAuthor().equalsIgnoreCase(author));
        if (publishedYear != null) stream = stream.filter(b -> b.getPublishedYear() == publishedYear);

        return stream
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }
    @GetMapping("/{id}")
    public Book getById(@PathVariable int id) {
        Book b = bookDB.get(id);
        if (b == null) throw new RuntimeException("Book not found with ID " + id);
        return b;
    }
    @PostMapping
    public ResponseEntity<String> addBook(@Valid @RequestBody Book book) {
        book.setId(idCounter++);
        bookDB.put(book.getId(), book);
        return ResponseEntity.ok("Book added.");
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateBook(@PathVariable int id, @Valid @RequestBody Book book) {
        if (!bookDB.containsKey(id)) return ResponseEntity.status(404).body("Book not found.");
        book.setId(id);
        bookDB.put(id, book);
        return ResponseEntity.ok("Book updated.");
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable int id) {
        if (bookDB.remove(id) == null) return ResponseEntity.status(404).body("Book not found.");
        return ResponseEntity.ok("Book deleted.");
    }
    @ControllerAdvice
    public static class ErrorHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("timestamp", LocalDateTime.now());
            error.put("status", 400);
            List<String> messages = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());
            error.put("errors", messages);
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, WebRequest req) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("timestamp", LocalDateTime.now());
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", ex.getMessage());
            err.put("path", req.getDescription(false).replace("uri=", ""));
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
    }
}
