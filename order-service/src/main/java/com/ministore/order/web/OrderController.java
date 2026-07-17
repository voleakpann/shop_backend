package com.ministore.order.web;

import com.ministore.order.client.ProductClient;
import com.ministore.order.client.ProductView;
import com.ministore.order.dto.CreateOrderRequest;
import com.ministore.order.dto.OrderItemRequest;
import com.ministore.order.model.Order;
import com.ministore.order.model.OrderItem;
import com.ministore.order.repository.OrderRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orders;
    private final ProductClient products;

    public OrderController(OrderRepository orders, ProductClient products) {
        this.orders = orders;
        this.products = products;
    }

    /** Places an order for the authenticated user. Requires a valid JWT. */
    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        Order order = new Order();

        // Identity always comes from the token, never from the request body.
        order.setUserEmail(jwt.getSubject());
        order.setStatus("PENDING");
        order.setCreatedAt(Instant.now());

        // Billing details from the checkout form.
        order.setCustomerName(request.customerName());
        order.setPhone(request.phone());
        order.setAddress(request.address());
        order.setCity(request.city());
        order.setStateRegion(request.stateRegion());
        order.setZip(request.zip());
        order.setNotes(request.notes());

        // Build line items from AUTHORITATIVE product data. The client only chose
        // the slug and quantity; price and name come from product-service, so a
        // tampered price in the request can't influence the total.
        List<OrderItem> items = new ArrayList<>();
        double total = 0;
        for (OrderItemRequest line : request.items()) {
            ProductView product = products.findBySlug(line.slug())
                    .orElseThrow(() -> new UnknownProductException(line.slug()));
            items.add(new OrderItem(product.slug(), product.name(), product.price(), line.qty()));
            total += product.price() * line.qty();
        }
        order.setItems(items);
        order.setTotal(total);

        Order saved = orders.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Lists the authenticated user's own orders. */
    @GetMapping
    public List<Order> myOrders(@AuthenticationPrincipal Jwt jwt) {
        return orders.findByUserEmailOrderByCreatedAtDesc(jwt.getSubject());
    }
}
