package com.booknest.wishlist.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlist_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId; // Changed to Integer

    @ManyToOne
    @JoinColumn(name = "wishlist_id", nullable = false)
    @JsonIgnore 
    private Wishlist wishlist;

    @Column(nullable = false)
    private Integer bookId; // Changed to Integer

    private String bookTitle; 
    
    private Double bookPrice; 
}