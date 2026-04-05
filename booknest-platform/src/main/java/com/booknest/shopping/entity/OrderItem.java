package com.booknest.shopping.entity;

import com.booknest.catalog.entity.Book;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELATIONSHIP: Many OrderItems belong to One Order.
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore // CRITICAL: Stops Spring from creating an infinite JSON loop (Order -> Item -> Order)
    private Order order;

    // RELATIONSHIP: Linking to the actual Book in the Catalog module.
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private Integer quantity;
    
    // THE SNAPSHOT RULE: 
    // We must save the exact price of the book at the millisecond of checkout.
    // If the Admin raises the book price to $2000 tomorrow, this user's receipt 
    // will still correctly show they only paid $1200.
    private Double priceAtPurchase; 
}