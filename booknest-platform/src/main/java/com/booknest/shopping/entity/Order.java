package com.booknest.shopping.entity;

import com.booknest.auth.entity.Address;
import com.booknest.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELATIONSHIP: Many orders can belong to One User.
    // nullable = false ensures an order can never exist without a buyer.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // RELATIONSHIP: Linking the Address entity from the Auth module.
    // This tells us exactly where to ship this specific order.
    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address shippingAddress;

    // The final calculated total of the entire order
    private Double totalAmount;
    
    // Tracks the lifecycle: "PLACED", "SHIPPED", "DELIVERED"
    private String orderStatus; 
    
    // Automatically captures the exact time the user clicked checkout
    private LocalDateTime orderDate;

    // RELATIONSHIP: One Order contains Many OrderItems.
    // CascadeType.ALL means if we delete the Order, the OrderItems are deleted too.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
}