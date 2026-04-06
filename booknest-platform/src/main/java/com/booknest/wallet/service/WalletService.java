package com.booknest.wallet.service;

import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.wallet.entity.Statement;
import com.booknest.wallet.entity.Wallet;
import com.booknest.wallet.repository.StatementRepository;
import com.booknest.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final StatementRepository statementRepository;
    private final UserRepository userRepository;

    // 1. Get Wallet (Auto-create if it doesn't exist)
    public Wallet getMyWallet(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return walletRepository.findByUser(user).orElseGet(() -> {
            Wallet newWallet = new Wallet();
            newWallet.setUser(user);
            newWallet.setCurrentBalance(0.0);
            return walletRepository.save(newWallet);
        });
    }

    // 2. Deposit Money (Top-Up)
    @Transactional
    public Wallet addMoney(String email, Double amount) {
        if (amount <= 0) throw new RuntimeException("Amount must be greater than zero");

        Wallet wallet = getMyWallet(email);
        wallet.setCurrentBalance(wallet.getCurrentBalance() + amount);
        Wallet updatedWallet = walletRepository.save(wallet);

        // Generate Statement Record
        Statement statement = new Statement();
        statement.setWallet(updatedWallet);
        statement.setTransactionType("DEPOSIT");
        statement.setAmount(amount);
        statement.setDateTime(LocalDateTime.now());
        statement.setTransactionRemarks("Added funds to wallet via Top-Up");
        statementRepository.save(statement);

        return updatedWallet;
    }

    // 3. Deduct Money (We will connect this to Order Checkout later)
    @Transactional
    public Wallet payMoney(String email, Double amount, Long orderId) {
        Wallet wallet = getMyWallet(email);

        if (wallet.getCurrentBalance() < amount) {
            throw new RuntimeException("Insufficient wallet balance! Please Top-Up.");
        }

        wallet.setCurrentBalance(wallet.getCurrentBalance() - amount);
        Wallet updatedWallet = walletRepository.save(wallet);

        // Generate Statement Record
        Statement statement = new Statement();
        statement.setWallet(updatedWallet);
        statement.setTransactionType("WITHDRAWAL");
        statement.setAmount(amount);
        statement.setDateTime(LocalDateTime.now());
        statement.setOrderId(orderId);
        statement.setTransactionRemarks("Paid for Order #" + orderId);
        statementRepository.save(statement);

        return updatedWallet;
    }

    // 4. View Statement History
    public List<Statement> getMyStatements(String email) {
        Wallet wallet = getMyWallet(email);
        return statementRepository.findByWalletOrderByDateTimeDesc(wallet);
    }
}