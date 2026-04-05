package com.booknest.shopping.service;

import com.booknest.auth.entity.Address;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.AddressRepository;
import com.booknest.auth.repository.UserRepository;
import com.booknest.catalog.entity.Book;
import com.booknest.catalog.repository.BookRepository;
import com.booknest.shopping.entity.Cart;
import com.booknest.shopping.entity.CartItem;
import com.booknest.shopping.entity.Order;
import com.booknest.shopping.entity.OrderItem;
import com.booknest.shopping.repository.CartRepository;
import com.booknest.shopping.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BookRepository bookRepository;

    // @Transactional is the most important annotation here!
    // It creates an "All-or-Nothing" bubble. 
    // If the server crashes at Step 4, it automatically "Rolls Back" Steps 1, 2, and 3 
    // so we don't accidentally deduct stock without saving the order.
    @Transactional 
    public Order placeOrder(String email, Long addressId) {
        
        // STEP 1: Security & Identity
        // Find who is buying, and where they want it shipped.
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new RuntimeException("Address not found"));
        
        // ANTI-HACKING CHECK: Ensure the user actually owns this address!
        if (!address.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized: Address does not belong to user.");
        }

        // STEP 2: Fetch the Cart
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Cart not found"));
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place order: Cart is empty.");
        }

        // STEP 3: Initialize the Blank Order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("PLACED");
        order.setTotalAmount(cart.getTotalPrice()); // Copy the total from the cart
        
        List<OrderItem> orderItems = new ArrayList<>();

        // STEP 4: The Loop - Deduct Stock & Create Items
        for (CartItem cartItem : cart.getItems()) {
            Book book = cartItem.getBook();
            
            // CONCURRENCY CHECK: Did someone else buy the last copy while this user was looking at their cart?
            if (book.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Out of stock: " + book.getTitle());
            }

            // Deduct the inventory in the warehouse
            book.setStockQuantity(book.getStockQuantity() - cartItem.getQuantity());
            bookRepository.save(book);

            // Convert the temporary CartItem into a permanent OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(book.getPrice()); // Take the price snapshot
            
            orderItems.add(orderItem);
        }

        // Save the completed order to the database
        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // STEP 5: Cleanup
        // Empty the cart so the user can start shopping again tomorrow
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);

        // Return the final receipt to the frontend
        return savedOrder;
    }
    
    // UC16: View Order History
    public List<Order> getOrderHistory(String email) {
        // 1. Find the user based on the email extracted from the JWT Token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 2. Fetch all orders for this specific user, automatically sorted newest-to-oldest
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
 // UC17: Admin - View All Orders
    // An admin needs to see every order in the system to process them.
    public List<Order> getAllOrdersForAdmin() {
        // Find all orders and sort them so the newest ones are at the top of the dashboard
        return orderRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "orderDate"));
    }

    // UC17: Admin - Update Order Status
    // Move an order from PLACED -> SHIPPED -> DELIVERED
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        // Update the status
        order.setOrderStatus(newStatus.toUpperCase());
        
        return orderRepository.save(order);
    }
}