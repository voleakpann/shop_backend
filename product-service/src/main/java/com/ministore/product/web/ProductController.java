package com.ministore.product.web;

import com.ministore.product.dto.CreateProductRequest;
import com.ministore.product.model.Product;
import com.ministore.product.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    /** Public: list all products, optionally filtered by category, brand or featured. */
    @GetMapping
    public List<Product> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Boolean featured) {
        if (featured != null && featured) {
            return products.findByFeaturedTrue();
        }
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) {
            return products.findByCategoryIgnoreCase(category);
        }
        if (brand != null && !brand.isBlank()) {
            return products.findByBrandIgnoreCase(brand);
        }
        return products.findAll();
    }

    /** Public: fetch a single product by its slug. */
    @GetMapping("/{slug}")
    public ResponseEntity<Product> bySlug(@PathVariable String slug) {
        return products.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Protected (requires a valid JWT): create a product. */
    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody CreateProductRequest request) {
        // Build the entity server-side: the client can't set the id, so this is
        // always an insert, never an accidental overwrite of an existing row.
        Product product = new Product(
                request.slug(),
                request.name(),
                request.price(),
                request.category(),
                request.tags(),
                request.brand(),
                request.image(),
                request.featured());
        Product saved = products.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Protected (requires a valid JWT): delete a product. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        products.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
