package com.booknest.shopping.repository;

import com.booknest.auth.entity.User;
import com.booknest.shopping.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // For UC16: View Order History
    List<Order> findByUserOrderByOrderDateDesc(User user);
}