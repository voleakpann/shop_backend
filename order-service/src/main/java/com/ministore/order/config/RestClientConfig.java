package com.ministore.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    /**
     * Load-balanced builder: "http://product-service" is resolved to a real
     * instance via Eureka. {@code @LoadBalanced} requires spring-cloud-loadbalancer,
     * which the Eureka client starter already pulls in.
     */
    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Pre-configured client for product-service. Connect/read timeouts are set so a
     * slow or hung product-service can't block order creation forever, and there are
     * no retries — a call fails fast rather than looping (KOSIGN CODE-002).
     */
    @Bean
    RestClient productRestClient(RestClient.Builder loadBalancedRestClientBuilder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        return loadBalancedRestClientBuilder
                .baseUrl("http://product-service")
                .requestFactory(factory)
                .build();
    }
}
