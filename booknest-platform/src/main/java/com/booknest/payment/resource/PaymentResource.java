package com.booknest.payment.resource;

import com.booknest.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentResource {

    private final PaymentService paymentService;

    // THE TRAFFIC COP: Handles both ONLINE and COD
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            Principal principal,
            @RequestParam Integer orderId, 
            @RequestParam Double amount,
            @RequestParam String paymentMethod) { 
        
        try {
            if ("COD".equalsIgnoreCase(paymentMethod)) {
                // Route 1: Process Cash on Delivery
                String result = paymentService.processCodOrder(orderId, amount, principal.getName());
                return ResponseEntity.ok(Map.of(
                        "status", "SUCCESS", 
                        "message", "COD Order Confirmed",
                        "action", result
                ));
            } 
            else if ("ONLINE".equalsIgnoreCase(paymentMethod)) {
                // Route 2: Generate Razorpay Order ID
                String razorpayOrderId = paymentService.createOnlineOrder(orderId, amount);
                return ResponseEntity.ok(Map.of(
                        "status", "PENDING_GATEWAY",
                        "razorpayOrderId", razorpayOrderId
                ));
            } 
            else {
                return ResponseEntity.badRequest().body("Invalid Payment Method Selected. Use 'ONLINE' or 'COD'.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Checkout failed: " + e.getMessage());
        }
    }

    // VERIFICATION ENDPOINT: Frontend hits this after Razorpay popup closes
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            Principal principal,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature) {
        
        boolean isSuccess = paymentService.verifyPayment(
                razorpayOrderId, 
                razorpayPaymentId, 
                razorpaySignature, 
                principal.getName() // Pass the email for the receipt
        );

        if (isSuccess) {
            return ResponseEntity.ok("Payment verified successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed. Invalid signature.");
        }
    }
}