package com.ministore.order.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

/**
 * Talks to product-service to fetch the authoritative price/name for a slug.
 * The GET catalogue endpoint is public, so no JWT is forwarded.
 */
@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(RestClient productRestClient) {
        this.restClient = productRestClient;
    }

    /** Returns the product for {@code slug}, or empty if product-service has no such slug. */
    public Optional<ProductView> findBySlug(String slug) {
        try {
            ProductView view = restClient.get()
                    .uri("/api/products/{slug}", slug)
                    .retrieve()
                    .body(ProductView.class);
            return Optional.ofNullable(view);
        } catch (RestClientResponseException ex) {
            // A 404 means "no such product" — a normal, expected outcome, not a failure.
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
    }
}
