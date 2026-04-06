package com.booknest.wallet.entity;

import com.booknest.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Corresponds to walletId in the case study diagram [cite: 378]

    // One User has One Wallet
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Double currentBalance = 0.0; // Starts at 0.0 [cite: 380]

    // A wallet has many statements. CascadeType.ALL ensures statements are saved/deleted with the wallet.
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Statement> statements = new ArrayList<>();
}