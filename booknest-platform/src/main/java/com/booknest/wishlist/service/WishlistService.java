package com.booknest.wishlist.service;

import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.wishlist.entity.Wishlist;
import com.booknest.wishlist.entity.WishlistItem;
import com.booknest.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository; 

    // FIXED: Resolving to Integer and using getUserId()
    private Integer resolveUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId(); // Fixed getter!
    }

    public Wishlist getMyWishlist(String email) { 
        Integer userId = resolveUserId(email);
        
        return wishlistRepository.findByUserId(userId).orElseGet(() -> {
            Wishlist newWishlist = new Wishlist();
            newWishlist.setUserId(userId);
            newWishlist.setCreatedAt(LocalDate.now());
            return wishlistRepository.save(newWishlist);
        });
    }

    @Transactional
    public Wishlist addBook(String email, Integer bookId, String bookTitle, Double bookPrice) { 
        Wishlist wishlist = getMyWishlist(email);

        boolean alreadyExists = wishlist.getBooks().stream()
                .anyMatch(item -> item.getBookId().equals(bookId));
        
        if (alreadyExists) {
            throw new RuntimeException("Book is already in your wishlist!");
        }

        WishlistItem newItem = new WishlistItem();
        newItem.setWishlist(wishlist);
        newItem.setBookId(bookId);
        newItem.setBookTitle(bookTitle);
        newItem.setBookPrice(bookPrice);

        wishlist.getBooks().add(newItem);
        return wishlistRepository.save(wishlist);
    }

    @Transactional
    public Wishlist removeBook(String email, Integer itemId) { 
        Wishlist wishlist = getMyWishlist(email);
        wishlist.getBooks().removeIf(item -> item.getItemId().equals(itemId));
        return wishlistRepository.save(wishlist);
    }

    @Transactional
    public Wishlist moveToCart(String email, Integer itemId) { 
        return removeBook(email, itemId);
    }

    @Transactional
    public void clearWishlist(String email) { 
        Wishlist wishlist = getMyWishlist(email);
        wishlist.getBooks().clear();
        wishlistRepository.save(wishlist);
    }
    
    public List<Wishlist> getAllWishlists() { 
        return wishlistRepository.findAll();
    }
}