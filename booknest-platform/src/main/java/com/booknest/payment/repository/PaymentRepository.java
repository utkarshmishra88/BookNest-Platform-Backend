package com.booknest.payment.repository;

import com.booknest.payment.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentRecord, Integer> {
    
    // Used during the verification step to fetch the pending online payment
    Optional<PaymentRecord> findByRazorpayOrderId(String razorpayOrderId);
}