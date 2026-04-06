package com.booknest.wishlist.resource;

import com.booknest.wishlist.entity.Wishlist;
import com.booknest.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistResource {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<Wishlist> getMyWishlist(Principal principal) { 
        return ResponseEntity.ok(wishlistService.getMyWishlist(principal.getName()));
    }

    @PostMapping("/add/{bookId}")
    public ResponseEntity<Wishlist> addBook(
            Principal principal,
            @PathVariable Integer bookId, // Changed to Integer
            @RequestParam String bookTitle,
            @RequestParam Double bookPrice) { 
        
        return ResponseEntity.ok(wishlistService.addBook(principal.getName(), bookId, bookTitle, bookPrice));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Wishlist> removeBook(Principal principal, @PathVariable Integer itemId) { // Changed to Integer
        return ResponseEntity.ok(wishlistService.removeBook(principal.getName(), itemId));
    }

    @PostMapping("/move-to-cart/{itemId}")
    public ResponseEntity<Wishlist> moveToCart(Principal principal, @PathVariable Integer itemId) { // Changed to Integer
        return ResponseEntity.ok(wishlistService.moveToCart(principal.getName(), itemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearWishlist(Principal principal) { 
        wishlistService.clearWishlist(principal.getName());
        return ResponseEntity.ok("Wishlist cleared successfully");
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Wishlist>> getAll() { 
        return ResponseEntity.ok(wishlistService.getAllWishlists());
    }
}