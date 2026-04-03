package com.booknest.auth.handler;

import com.booknest.auth.entity.User;
import com.booknest.auth.service.AuthService;
import com.booknest.auth.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Intercepts the request right after GitHub says "Yes, this is a valid user"
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // 1. Extract the user data that GitHub sent us
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        
        // Fallback: If their GitHub email is set to private, use their login handle
        if (email == null) {
            email = oAuth2User.getAttribute("login") + "@github.com"; 
        }

        // 2. Save or fetch the user from our MySQL database
        User user = authService.processOAuthPostLogin(email, name);

        // 3. Generate our BookNest VIP pass (JWT)
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // 4. Send the token back to the browser screen
        response.setContentType("application/json");
        response.getWriter().write("{\"token\": \"" + token + "\", \"message\": \"GitHub Login Successful\"}");
    }
}