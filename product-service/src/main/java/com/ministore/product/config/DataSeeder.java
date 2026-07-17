package com.ministore.product.config;

import com.ministore.product.model.Product;
import com.ministore.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Seeds the same products used in the frontend's lib/data.ts on first run. */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                return; // already seeded
            }
            repo.saveAll(List.of(
                    new Product("iphone-10", "iPhone 10", 980, "Phones", List.of("White", "Modern"), "Apple", "/images/product-item1.jpg", true),
                    new Product("iphone-11", "iPhone 11", 1100, "Phones", List.of("White", "Modern"), "Apple", "/images/iphone12.jpg", false),
                    new Product("iphone-16", "iPhone 16", 780, "Phones", List.of("White", "Cheap"), "Apple", "/images/iphone_.jpg", false),
                    new Product("pink-watch", "Pink Watch", 870, "Watches", List.of("Classic", "Modern"), "Apple", "/images/product-item6.jpg", true),
                    new Product("heavy-watch", "Heavy Watch", 680, "Watches", List.of("Modern"), "Samsung", "/images/product-item7.jpg", false),
                    new Product("spotted-watch", "Spotted Watch", 750, "Watches", List.of("Classic"), "Green", "/images/product-item8.jpg", false),
                    new Product("spotted-watch-2", "Spotted Watch", 750, "Watches", List.of("Classic"), "Green", "/images/product-item9.jpg", false),
                    new Product("black-watch", "Black Watch", 450, "Watches", List.of("Modern"), "Samsung", "/images/product-item10.jpg", false),
                    new Product("iphone-13", "iPhone 13", 1500, "Phones", List.of("Modern", "White"), "Apple", "/images/product-item4.jpg", true),
                    new Product("iphone-12", "iPhone 12", 1300, "Phones", List.of("Modern"), "Apple", "/images/product-item5.jpg", false)
            ));
        };
    }
}
