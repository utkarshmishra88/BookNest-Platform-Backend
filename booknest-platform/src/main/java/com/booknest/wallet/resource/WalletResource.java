
package com.booknest.wallet.resource;

import com.booknest.wallet.entity.Statement;
import com.booknest.wallet.entity.Wallet;
import com.booknest.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletResource {

    private final WalletService walletService;

    // View current wallet balance
    @GetMapping
    public ResponseEntity<Wallet> getWallet(Principal principal) {
        return ResponseEntity.ok(walletService.getMyWallet(principal.getName()));
    }

    // Add money to wallet
    @PostMapping("/deposit")
    public ResponseEntity<Wallet> addMoney(Principal principal, @RequestParam Double amount) {
        return ResponseEntity.ok(walletService.addMoney(principal.getName(), amount));
    }

    // View transaction history
    @GetMapping("/statements")
    public ResponseEntity<List<Statement>> getStatements(Principal principal) {
        return ResponseEntity.ok(walletService.getMyStatements(principal.getName()));
    }
}