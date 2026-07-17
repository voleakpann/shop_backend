package com.ministore.order.model;

import jakarta.persistence.Embeddable;

/** A single line in an order. Stored inline with the order. */
@Embeddable
public class OrderItem {

    private String slug;
    private String name;
    private double price;
    private int qty;

    public OrderItem() {
    }

    public OrderItem(String slug, String name, double price, int qty) {
        this.slug = slug;
        this.name = name;
        this.price = price;
        this.qty = qty;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}
