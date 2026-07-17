package com.ministore.product.repository;

import com.ministore.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByBrandIgnoreCase(String brand);

    List<Product> findByFeaturedTrue();
}
