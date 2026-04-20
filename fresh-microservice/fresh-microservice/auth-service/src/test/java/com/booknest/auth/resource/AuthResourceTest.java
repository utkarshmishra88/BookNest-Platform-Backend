package com.booknest.auth.resource;

import com.booknest.auth.dto.LoginRequest;
import com.booknest.auth.dto.OtpVerifyRequest;
import com.booknest.auth.dto.RegisterRequest;
import com.booknest.auth.entity.User;
import com.booknest.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthResource authResource;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authResource).build();
    }

    @Test
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(authService.registerUser(any(User.class))).thenReturn("Registration successful! Please check your email for the OTP.");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Registration successful! Please check your email for the OTP."));
    }

    @Test
    void testVerify() throws Exception {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setEmail("john@example.com");
        request.setOtp("123456");

        when(authService.verifyOtp(anyString(), anyString())).thenReturn("Account verified! You can now login.");

        mockMvc.perform(post("/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account verified! You can now login."));
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(authService.login(anyString(), anyString())).thenReturn("mockJwtToken");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("mockJwtToken"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer mockJwtToken"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully."));
    }
}
