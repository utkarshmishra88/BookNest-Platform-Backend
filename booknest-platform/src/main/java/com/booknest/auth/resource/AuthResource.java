package com.booknest.auth.resource;

import com.booknest.auth.dto.AuthResponse;
import com.booknest.auth.dto.LoginRequest;
import com.booknest.auth.dto.PasswordChangeRequest;
import com.booknest.auth.dto.ProfileUpdateRequest;
import com.booknest.auth.entity.User;
import com.booknest.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// The receptionist that catches incoming HTTP requests.
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    // @Valid makes sure the incoming JSON isn't missing required fields like email
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = authService.register(user);
            
            // Security first: Scrub the password hash before replying to the client
            registeredUser.setPasswordHash(null); 
            
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
            
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
 // Handles POST requests to authenticate users
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Service returns the raw token string
            String token = authService.login(loginRequest);
            
            // Package the token into our clean DTO format
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .message("Login successful")
                    .build());
                    
        } catch (RuntimeException e) {
            // Block access and return a generic 401 message for security
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
    
 // UC05: Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        
        // 1. Wipe the Bouncer's memory for this specific request
        SecurityContextHolder.clearContext();
        
        // 2. Send the success signal back to the client
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", "Logged out successfully. The client must now delete the JWT from local storage."
        ));
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile() {
        
        // 1. Get the current user's email from the Security Context (placed there by the UC04 JWT Filter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();

        // 2. Fetch the full user details from the database
        User user = authService.getUserByEmail(email);

        // 3. Return the user data (Spring will automatically convert the User object to JSON)
        return ResponseEntity.ok(user);
    }
    
    // UC08 - Part 1: Update Profile Details
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request) {
        // Extract email from JWT
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        User updatedUser = authService.updateProfile(email, request);
        return ResponseEntity.ok(updatedUser);
    }

    // UC08 - Part 2: Change Password
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request) {
        // Extract email from JWT
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        authService.changePassword(email, request);
        
        return ResponseEntity.ok(java.util.Map.of(
            "status", "SUCCESS",
            "message", "Password updated successfully!"
        ));
    }
}

//ResponseEntity is a built-in Spring Boot class that wraps your entire response. 
//It allows you to control the Body (the actual data, like the user object or error message), 
//the Headers, and the Status Code all at once.

//SecurityContextHolder.clearContext();
//In UC04, your JWT Filter intercepts the token and writes the user's 
//email into the SecurityContextHolder (Spring's short-term memory for that exact 
//		HTTP request).
//
//Calling clearContext() instantly wipes that memory. It tells Spring, 
//"Forget who was just talking to you." #### 3. ResponseEntity.ok(Map.of(...))
//
//We return a standard JSON object containing a status and a message.
//
//When your React/Angular developer builds the frontend, they will write 
//code that says: "If the backend responds with SUCCESS, delete the"
//		+ " JWT from the browser."