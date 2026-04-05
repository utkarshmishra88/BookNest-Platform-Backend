package com.booknest.shopping.repository;

import com.booknest.auth.entity.User;
import com.booknest.shopping.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
	
    Optional<Cart> findByUser(User user);
}