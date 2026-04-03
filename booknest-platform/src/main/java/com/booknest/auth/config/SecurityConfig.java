package com.booknest.auth.config;

import com.booknest.auth.filter.JwtAuthenticationFilter; // Import added
import com.booknest.auth.handler.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // Inject the filter
    private final OAuth2SuccessHandler oAuth2SuccessHandler; 

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/register", "/auth/login", "/error").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/books/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
            )
            // CRITICAL: Tells Spring to use our Filter before its own internal filters
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Now we can go STATELESS because our filter handles every request
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}

//.addFilterBefore(...): This is crucial. We tell Spring, 
//"Run my JWT Bouncer before you run your own internal Username/Password checks."
//
//STATELESS Policy: Since we are using JWTs, we tell Spring not to create
//"Sessions" (cookies). Every request must stand on its own by providing a token.