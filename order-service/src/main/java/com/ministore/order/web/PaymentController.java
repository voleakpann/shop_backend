package com.ministore.order.web;

import com.ministore.order.model.Order;
import com.ministore.order.repository.OrderRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final OrderRepository orders;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public PaymentController(OrderRepository orders) {
        this.orders = orders;
    }

    /** Creates a Stripe PaymentIntent for one of the caller's own orders so the card form can be shown inline. */
    @PostMapping("/{id}/payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@PathVariable Long id,
                                                                     @AuthenticationPrincipal Jwt jwt) throws StripeException {
        Order order = orders.findById(id)
                .filter(o -> o.getUserEmail().equals(jwt.getSubject()))
                .orElseThrow(() -> new OrderNotFoundException(id));

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(Math.round(order.getTotal() * 100))
                .setCurrency("usd")
                .setDescription("Order #" + order.getId() + " — MiniStore")
                .setReceiptEmail(order.getUserEmail())
                .putMetadata("orderId", String.valueOf(order.getId()))
                .putMetadata("customerEmail", order.getUserEmail())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        order.setStripePaymentIntentId(intent.getId());
        orders.save(order);

        return ResponseEntity.ok(Map.of("clientSecret", intent.getClientSecret()));
    }

    /** Stripe calls this once a payment finishes. No JWT here — authenticity comes from the Stripe-Signature header instead. */
    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request,
                                                        @RequestHeader("Stripe-Signature") String signature) throws IOException {
        // Must be the exact raw bytes Stripe signed — reading via a Reader/lines()
        // can subtly alter whitespace and break signature verification.
        String payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        }

        log.info("Stripe webhook received: {}", event.getType());

        if ("payment_intent.succeeded".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                if (obj instanceof PaymentIntent intent) {
                    String orderId = intent.getMetadata().get("orderId");
                    if (orderId != null) {
                        orders.findById(Long.valueOf(orderId)).ifPresent(order -> {
                            order.setStatus("PAID");
                            orders.save(order);
                            log.info("Order {} marked PAID", orderId);
                        });
                    }
                }
            });
        }

        return ResponseEntity.ok("");
    }
}
