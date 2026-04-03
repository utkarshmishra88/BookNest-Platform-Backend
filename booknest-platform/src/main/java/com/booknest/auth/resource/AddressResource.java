package com.booknest.auth.resource;

import com.booknest.auth.entity.Address;
import com.booknest.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/addresses")
@RequiredArgsConstructor
public class AddressResource {

    private final AuthService authService;

    // Helper method to get the logged-in user's email from the JWT
    private String getAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getPrincipal();
    }

    // 1. Add a new address
    @PostMapping
    public ResponseEntity<Address> addAddress(@RequestBody Address address) {
        String email = getAuthenticatedEmail();
        Address savedAddress = authService.addAddress(email, address);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
    }

    // 2. View all my addresses
    @GetMapping
    public ResponseEntity<List<Address>> getMyAddresses() {
        String email = getAuthenticatedEmail();
        List<Address> addresses = authService.getUserAddresses(email);
        return ResponseEntity.ok(addresses);
    }
}