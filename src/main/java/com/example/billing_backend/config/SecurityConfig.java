package com.example.billing_backend.config;

import com.example.billing_backend.exception.CustomAccessDeniedHandler;
import com.example.billing_backend.exception.CustomAuthEntryPoint;
import com.example.billing_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Frontend URL connection
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Open Endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. STOCK ALERTS (Real-time monitoring)
                        .requestMatchers("/api/stock-alerts/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "CASHIER", "ROLE_CASHIER")

                        // 3. INVENTORY MANAGEMENT
                        .requestMatchers("/api/inventory/add/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/api/inventory/reduce/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "CASHIER", "ROLE_CASHIER")
                        .requestMatchers("/api/inventory/reorder-level/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/api/inventory/low-stock").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "CASHIER", "ROLE_CASHIER")
                        .requestMatchers("/api/inventory/product/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "CASHIER", "ROLE_CASHIER")

                        // 4. ADMIN & BILLING ROUTES
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/api/billing/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "CASHIER", "ROLE_CASHIER")
                        .requestMatchers("/api/cart/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "CASHIER", "ROLE_CASHIER")

                        // 5. Secure all other requests
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}