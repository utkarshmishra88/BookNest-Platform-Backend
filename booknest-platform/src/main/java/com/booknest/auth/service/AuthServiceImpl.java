package com.booknest.auth.service;

import com.booknest.auth.dto.LoginRequest;
import com.booknest.auth.dto.PasswordChangeRequest;
import com.booknest.auth.dto.ProfileUpdateRequest;
import com.booknest.auth.entity.Address;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.AddressRepository;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// Where the heavy lifting and business rules happen.
@Service
@RequiredArgsConstructor 
public class AuthServiceImpl implements AuthService {

    // Constructor injection via Lombok keeps things clean
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final JwtUtil jwtUtil;

    @Override
    public User register(User user) {
        
        // Fail fast if they are already in the system
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with email " + user.getEmail() + " already exists!");
        }

        // Hash the password before it touches the database
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        // Set some sensible defaults for new signups
        if (user.getRole() == null) user.setRole("CUSTOMER");
        if (user.getProvider() == null) user.setProvider("LOCAL");
        
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
    
    @Override
    public String login(LoginRequest request) {
        // 1. Find the user. Throw exception if they don't exist.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Verify the raw password typed in Postman against the MySQL hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Success! Mint and return the secure JWT.
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }
    
    @Override
    public User processOAuthPostLogin(String email, String name) {
        
        // 1. Check if they already exist in our database
        return userRepository.findByEmail(email).orElseGet(() -> {
            
            // 2. If they are a brand new user, auto-register them
            User newUser = User.builder()
                    .email(email)
                    .fullName(name != null ? name : "GitHub User")
                    // They log in via GitHub, so they don't have a local password here
                    .passwordHash("OAUTH_USER_NO_PASSWORD") 
                    .role("CUSTOMER")
                    .provider("GITHUB") // Tag them as a GitHub user
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return userRepository.save(newUser);
        });
    }
    
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    // 1. Inject the new repository
    private final AddressRepository addressRepository;

    // 2. Add the implementation methods
    @Override
    public Address addAddress(String email, Address address) {
        User user = getUserByEmail(email); // Reusing the method we built in UC06
        address.setUser(user);             // Link the address to the user
        return addressRepository.save(address);
    }

    @Override
    public java.util.List<Address> getUserAddresses(String email) {
        User user = getUserByEmail(email);
        return addressRepository.findByUser(user);
    }
    
    @Override
    public User updateProfile(String email, ProfileUpdateRequest request) {
        User user = getUserByEmail(email);
        
        // Update fields only if they are provided in the request
        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getMobile() != null) {
            user.setMobile(request.getMobile());
        }
        
        return userRepository.save(user);
    }

    @Override
    public void changePassword(String email, PasswordChangeRequest request) {
        User user = getUserByEmail(email);

        // Security Check 1: OAuth users don't have local passwords
        if (!user.getProvider().equals("LOCAL")) {
            throw new RuntimeException("Social login users cannot change their password here.");
        }

        // Security Check 2: Does the old password match the database?
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Incorrect old password!");
        }

        // Security Check 3: Encode the new password and save
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}