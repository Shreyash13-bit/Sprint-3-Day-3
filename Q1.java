package nisum.ascend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@SpringBootApplication
@RestController
@RequestMapping("/products")
public class Q1 {
    private List<Product> products = new ArrayList<>();
    public static void main(String[] args) {
        SpringApplication.run(ProductApiApplication.class, args);
    }
    static class Product {
        private int id;
        private String name;
        private double price;
        public Product() {}
        public Product(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
    @GetMapping
    public List<Product> getAll() {
        return products;
    }
    @GetMapping("/{id}")
    public Product getById(@PathVariable int id) {
        for (Product p : products) {
            if (p.getId() == id)
                return p;
        }
        return null;
    }
    @PostMapping
    public String add(@RequestBody Product p) {
        products.add(p);
        return "Added.";
    }
    @PutMapping("/{id}")
    public String update(@PathVariable int id, @RequestBody Product p) {
        for (Product prod : products) {
            if (prod.getId() == id) {
                prod.setName(p.getName());
                prod.setPrice(p.getPrice());
                return "Updated.";
            }
        }
        return "Not found.";
    }
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        boolean removed = products.removeIf(p -> p.getId() == id);
        return removed ? "Deleted." : "Not found.";
    }
}
