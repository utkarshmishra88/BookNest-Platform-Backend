package com.booknest.wishlist.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wishlists")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer wishlistId; // Changed to Integer

    @Column(nullable = false, unique = true)
    private Integer userId; // Changed to Integer

    private LocalDate createdAt; 

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WishlistItem> books = new ArrayList<>(); 
}