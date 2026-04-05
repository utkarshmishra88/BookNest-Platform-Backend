package com.booknest.shopping.resource;

import com.booknest.shopping.entity.Cart;
import com.booknest.shopping.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartResource {

    private final CartService cartService;

    // 1. View My Cart
    @GetMapping
    public ResponseEntity<Cart> getMyCart(Principal principal) {
        // Principal automatically holds the email from the JWT token!
        return ResponseEntity.ok(cartService.getCartByUserEmail(principal.getName()));
    }

    // 2. Add Item to Cart
    @PostMapping("/add/{bookId}")
    public ResponseEntity<Cart> addToCart(
            Principal principal, 
            @PathVariable Long bookId, 
            @RequestParam(defaultValue = "1") int quantity) {
        
        return ResponseEntity.ok(cartService.addItemToCart(principal.getName(), bookId, quantity));
    }

    // 3. Remove Item from Cart
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Cart> removeFromCart(Principal principal, @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(principal.getName(), itemId));
    }
}