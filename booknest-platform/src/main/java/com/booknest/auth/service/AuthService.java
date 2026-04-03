package com.booknest.auth.service;

import com.booknest.auth.dto.LoginRequest;
import com.booknest.auth.dto.PasswordChangeRequest;
import com.booknest.auth.dto.ProfileUpdateRequest;
import com.booknest.auth.entity.Address;
import com.booknest.auth.entity.User;

public interface AuthService {
    User register(User user);
    
    String login(LoginRequest loginRequest);
    
    User processOAuthPostLogin(String email, String name);
    
    User getUserByEmail(String email);
    
    Address addAddress(String email, Address address);
    
    java.util.List<Address> getUserAddresses(String email);
    
    User updateProfile(String email, ProfileUpdateRequest request);
    
    void changePassword(String email, PasswordChangeRequest request);
}