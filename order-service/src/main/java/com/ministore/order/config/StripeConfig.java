package com.ministore.order.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        // KOSIGN CODE-002: every external API call must have a timeout and must never
        // retry forever. Stripe ships with defaults, but we pin them explicitly.
        Stripe.setConnectTimeout(10_000);  // 10s to open the connection
        Stripe.setReadTimeout(30_000);     // 30s to read the response
        Stripe.setMaxNetworkRetries(2);    // bounded retries, never infinite
    }
}
