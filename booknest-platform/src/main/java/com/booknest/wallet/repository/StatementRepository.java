package com.booknest.wallet.repository;

import com.booknest.wallet.entity.Statement;
import com.booknest.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StatementRepository extends JpaRepository<Statement, Long> {
    // To fetch statement history for a specific wallet, ordered by newest first
    List<Statement> findByWalletOrderByDateTimeDesc(Wallet wallet);
}