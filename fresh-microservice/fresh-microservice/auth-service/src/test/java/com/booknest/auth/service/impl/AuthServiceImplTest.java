package com.booknest.auth.service.impl;

import com.booknest.auth.client.NotificationClient;
import com.booknest.auth.dto.UserResponse;
import com.booknest.auth.entity.TokenBlacklist;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.TokenBlacklistRepository;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .userId(1)
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash("password123")
                .role("CUSTOMER")
                .active(true)
                .build();
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        String response = authService.registerUser(validUser);

        assertEquals("Registration successful! Please check your email for the OTP.", response);
        verify(userRepository, times(1)).save(any(User.class));
        verify(notificationClient, times(1)).sendOtp(eq("john@example.com"), anyString(), any());
    }

    @Test
    void testRegisterUser_MissingFullName() {
        validUser.setFullName("");
        Exception exception = assertThrows(RuntimeException.class, () -> authService.registerUser(validUser));
        assertEquals("Full name is required.", exception.getMessage());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        Exception exception = assertThrows(RuntimeException.class, () -> authService.registerUser(validUser));
        assertEquals("Email already exists!", exception.getMessage());
    }

    @Test
    void testVerifyOtp_Success() {
        validUser.setOtp("123456");
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));

        String response = authService.verifyOtp("john@example.com", "123456");

        assertEquals("Account verified! You can now login.", response);
        assertTrue(validUser.getActive());
        assertNull(validUser.getOtp());
        verify(userRepository, times(1)).save(validUser);
    }

    @Test
    void testVerifyOtp_InvalidOtp() {
        validUser.setOtp("123456");
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.verifyOtp("john@example.com", "wrongOtp"));
        assertEquals("Invalid OTP.", exception.getMessage());
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mockJwtToken");

        String token = authService.login("john@example.com", "password123");

        assertEquals("mockJwtToken", token);
    }

    @Test
    void testLogin_AccountNotActive() {
        validUser.setActive(false);
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.login("john@example.com", "password123"));
        assertTrue(exception.getReason().contains("Please verify your account via OTP first."));
    }

    @Test
    void testLogin_InvalidCredentials() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.login("john@example.com", "wrongPassword"));
        assertTrue(exception.getReason().contains("Invalid email or password."));
    }

    @Test
    void testLogout_Success() {
        when(tokenBlacklistRepository.existsByToken(anyString())).thenReturn(false);
        
        authService.logout("Bearer mockJwtToken");

        verify(tokenBlacklistRepository, times(1)).save(any(TokenBlacklist.class));
    }

    @Test
    void testUpdateProfile_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        User updated = authService.updateProfile(1, "1", "Jane Doe", "+919876543210");

        assertEquals("Jane Doe", updated.getFullName());
        assertEquals("+919876543210", updated.getMobileNumber());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateProfile_Forbidden() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> authService.updateProfile(1, "2", "Jane Doe", "+919876543210"));
        assertTrue(exception.getReason().contains("You can only update your own profile."));
    }

    @Test
    void testRequestEmailChange_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        String response = authService.requestEmailChange(1, "1", "new@example.com");

        assertTrue(response.contains("Verification code sent to new@example.com"));
        assertEquals("new@example.com", validUser.getPendingEmail());
        assertNotNull(validUser.getEmailChangeOtp());
        verify(notificationClient, times(1)).sendOtp(eq("new@example.com"), anyString(), any());
    }

    @Test
    void testConfirmEmailChange_Success() {
        validUser.setPendingEmail("new@example.com");
        validUser.setEmailChangeOtp("123456");
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("newJwtToken");

        String token = authService.confirmEmailChange(1, "1", "new@example.com", "123456");

        assertEquals("newJwtToken", token);
        assertEquals("new@example.com", validUser.getEmail());
        assertNull(validUser.getPendingEmail());
        assertNull(validUser.getEmailChangeOtp());
    }

    @Test
    void testForgotPassword_Success() {
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(validUser));
        when(jwtUtil.generateToken(any(), anyLong())).thenReturn("resetToken");

        String response = authService.forgotPassword("john@example.com");

        assertEquals("Password reset link has been sent to your email.", response);
        verify(notificationClient, times(1)).sendUpdate(eq("john@example.com"), anyString(), anyString(), any());
    }

    @Test
    void testResetPassword_Success() {
        when(jwtUtil.validateToken("resetToken")).thenReturn(true);
        when(jwtUtil.extractClaim(eq("resetToken"), any())).thenReturn("PASSWORD_RESET");
        when(jwtUtil.extractEmail("resetToken")).thenReturn("john@example.com");
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(validUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");

        String response = authService.resetPassword("resetToken", "newPassword123");

        assertEquals("Password successfully reset! You can now log in.", response);
        assertEquals("newHashedPassword", validUser.getPasswordHash());
        verify(tokenBlacklistRepository, times(1)).save(any(TokenBlacklist.class));
    }
}
