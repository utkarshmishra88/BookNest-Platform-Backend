package com.booknest.wallet.repository;

import com.booknest.auth.entity.User;
import com.booknest.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
	
    Optional<Wallet> findByUser(User user);
}