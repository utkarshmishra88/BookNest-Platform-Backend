package com.booknest.auth.resource;

import com.booknest.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 callback endpoint.
 *
 * After successful OAuth2 login Spring Security will redirect here (configured in SecurityConfig).
 * This endpoint issues a JWT and then redirects the browser back to the frontend with the token.
 */
@RestController
@RequiredArgsConstructor
public class OAuth2Controller {

    private final JwtUtil jwtUtil;

    // Frontend callback URL — adjust if you prefer gateway host
    private final String FRONTEND_CALLBACK = "http://localhost:5173/oauth/callback";

    @GetMapping("/oauth2/success")
    public ResponseEntity<Void> oauth2Success(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> attributes = new HashMap<>(principal.getAttributes());

        // Normalize claims
        Map<String, Object> claims = new HashMap<>();
        Object email = attributes.get("email");
        Object name = attributes.get("name");
        Object sub = attributes.get("sub"); // provider id

        claims.put("email", email != null ? email : sub);
        claims.put("name", name != null ? name : attributes.getOrDefault("login", sub));
        claims.put("role", "CUSTOMER");
        claims.put("providerAttributes", attributes);

        // Optionally: persist or upsert user in your DB here using UserRepository
        // e.g. findByEmail(...) then create/update User entity and save
        // After saving, you can generate token using JwtUtil.generateToken(User) instead

        String token = jwtUtil.generateToken(claims);

        // Redirect to frontend with token (URL-encoded)
        String redirectUrl = FRONTEND_CALLBACK + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }
}