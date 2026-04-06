package com.booknest.payment.service;

import com.booknest.payment.entity.PaymentRecord;
import com.booknest.payment.repository.PaymentRepository;
import com.booknest.utils.NotificationEvent;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    // --------------------------------------------------------
    // ROUTE 1: ONLINE PAYMENT (Initiates Razorpay Handshake)
    // --------------------------------------------------------
    @Transactional
    public String createOnlineOrder(Integer localOrderId, Double amount) throws Exception {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", Math.round(amount * 100)); // Razorpay uses Paisa
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_rcptid_" + localOrderId);

        Order razorpayOrder = client.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        PaymentRecord record = PaymentRecord.builder()
                .razorpayOrderId(razorpayOrderId)
                .orderId(localOrderId)
                .amount(amount)
                .paymentMethod("ONLINE")
                .status("CREATED")
                .timestamp(LocalDateTime.now())
                .build();
        paymentRepository.save(record);

        return razorpayOrderId;
    }

    // --------------------------------------------------------
    // ROUTE 2: CASH ON DELIVERY (Bypasses Razorpay)
    // --------------------------------------------------------
    @Transactional
    public String processCodOrder(Integer localOrderId, Double amount, String userEmail) {
        PaymentRecord record = PaymentRecord.builder()
                .orderId(localOrderId)
                .amount(amount)
                .paymentMethod("COD")
                .status("PENDING_DELIVERY")
                .timestamp(LocalDateTime.now())
                .build();
        paymentRepository.save(record);

        // Fire the email receipt immediately for COD
        String receiptMsg = "Your Cash on Delivery Order #" + localOrderId + " for Rs. " + amount + " has been confirmed! Please keep exact change ready at the time of delivery.";
        eventPublisher.publishEvent(new NotificationEvent(userEmail, "ORDER_CONFIRMED", receiptMsg));

        return "COD_CONFIRMED";
    }

    // --------------------------------------------------------
    // VERIFICATION: Only used for ONLINE payments
    // --------------------------------------------------------
    @Transactional
    public boolean verifyPayment(String orderId, String paymentId, String signature, String userEmail) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (isValid) {
                PaymentRecord record = paymentRepository.findByRazorpayOrderId(orderId)
                        .orElseThrow(() -> new RuntimeException("Payment record not found"));
                
                record.setRazorpayPaymentId(paymentId);
                record.setRazorpaySignature(signature);
                record.setStatus("SUCCESS");
                paymentRepository.save(record);

                // Publish a background event to send the email receipt!
                String receiptMsg = "Payment Successful! Rs. " + record.getAmount() + " has been received for Order #" + record.getOrderId() + ". Reference ID: " + paymentId;
                eventPublisher.publishEvent(new NotificationEvent(userEmail, "PAYMENT_SUCCESS", receiptMsg));

                return true;
            }
        } catch (Exception e) {
            System.err.println("Signature Verification Exception: " + e.getMessage());
        }
        return false;
    }
}