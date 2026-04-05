package com.booknest.shopping.resource;

import com.booknest.shopping.entity.Order;
import com.booknest.shopping.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderResource {

    private final OrderService orderService;

    // Endpoint: POST /api/orders/checkout/{addressId}
    @PostMapping("/checkout/{addressId}")
    public ResponseEntity<Order> checkout(Principal principal, @PathVariable Long addressId) {
        
        // The 'Principal' object is magic from Spring Security.
        // It automatically reads the Bearer Token from the request header 
        // and extracts the logged-in user's email. 
        // This means the frontend never has to send the User ID, preventing hackers from spoofing IDs.
        Order completedOrder = orderService.placeOrder(principal.getName(), addressId);
        
        // Return 201 Created along with the final order details
        return ResponseEntity.status(HttpStatus.CREATED).body(completedOrder);
    }
    
    // UC16: Get My Order History
    @GetMapping("/history")
    public ResponseEntity<List<Order>> getMyOrders(Principal principal) {
        
        // The Principal object securely holds the logged-in user's email.
        // There is no way for a hacker to pass "?userId=2" in the URL and see someone else's orders!
        List<Order> history = orderService.getOrderHistory(principal.getName());
        
        return ResponseEntity.ok(history);
    }
    
 // UC17: Admin Dashboard - Get All Orders
    @GetMapping("/admin/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
    }

    // UC17: Admin - Update Status
    // Example URL: PUT /api/orders/admin/1/status?newStatus=SHIPPED
    @PutMapping("/admin/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId, 
            @RequestParam String newStatus) {
        
        Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }
}