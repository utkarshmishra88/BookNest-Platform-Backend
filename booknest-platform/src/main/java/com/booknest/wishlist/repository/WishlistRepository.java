package com.booknest.wishlist.repository;

import com.booknest.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> { // Changed to Integer
    
    Optional<Wishlist> findByUserId(Integer userId);
    
    boolean existsByUserId(Integer userId);
    
    void deleteByUserId(Integer userId);
}