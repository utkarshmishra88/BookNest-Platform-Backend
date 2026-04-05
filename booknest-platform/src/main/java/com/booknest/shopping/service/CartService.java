package com.booknest.shopping.service;

import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository; // Assuming your UC01 repo
import com.booknest.catalog.entity.Book;
import com.booknest.catalog.repository.BookRepository;
import com.booknest.shopping.entity.Cart;
import com.booknest.shopping.entity.CartItem;
import com.booknest.shopping.repository.CartRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    // Get cart (or create a blank one if it doesn't exist yet)
    public Cart getCartByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    public Cart addItemToCart(String email, Long bookId, int quantity) {
        Cart cart = getCartByUserEmail(email);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available!");
        }

        // Check if book is already in the cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update existing item
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setSubTotal(item.getQuantity() * book.getPrice());
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setQuantity(quantity);
            newItem.setSubTotal(quantity * book.getPrice());
            cart.getItems().add(newItem);
        }
        
        recalculateCartTotal(cart);
        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(String email, Long itemId) {
        Cart cart = getCartByUserEmail(email);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        recalculateCartTotal(cart);
        return cartRepository.save(cart);
    }

    private void recalculateCartTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();
        cart.setTotalPrice(total);
    }
}