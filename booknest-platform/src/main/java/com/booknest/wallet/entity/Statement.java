package com.booknest.wallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "statements")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Corresponds to statementId

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    @JsonIgnore // Prevent infinite JSON loops when returning wallet data
    private Wallet wallet;

    private String transactionType; // e.g., "DEPOSIT" or "WITHDRAWAL"
    
    private Double amount;
    
    private LocalDateTime dateTime;
    
    // Optional: If this was a purchase, link it to the Order ID
    private Long orderId; 
    
    private String transactionRemarks; // e.g., "Added funds via Credit Card" or "Paid for Order #5"
}