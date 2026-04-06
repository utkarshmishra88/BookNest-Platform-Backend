package com.booknest.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // These will be null if the user chooses COD
    @Column(unique = true)
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    
    @Column(nullable = false)
    private Integer orderId; // Links back to the Order Service

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String paymentMethod; // "ONLINE" or "COD"

    @Column(nullable = false)
    private String status; // CREATED, SUCCESS, FAILED, PENDING_DELIVERY

    private LocalDateTime timestamp;
}