
package com.ticketflow.backend.config;

import com.ticketflow.backend.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:3000", "http://localhost:5173", "http://localhost:5174")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH",
                        "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {

        http
            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource()
            ))
            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

    .requestMatchers(
        "/api/auth/login",
        "/api/auth/register",
        "/api/health",
        "/api/ai/**",
        "/api/coupons/validate"
    ).permitAll()

    .requestMatchers(
        HttpMethod.GET,
        "/api/events",
        "/api/events/**",
        "/api/venues",
        "/api/venues/**",
        "/api/event-seats/**",
        "/api/event-stalls/**",
        "/api/stall-items/**",
        "/api/stalls/**",
        "/api/reviews/event/**"
    ).permitAll()

    .requestMatchers(
        "/api/auth/users",
        "/api/auth/users/**",
        "/api/bookings/admin/**",
        "/api/activity-logs",
        "/api/activity-logs/**"
    ).hasRole("ADMIN")

    .requestMatchers(
        HttpMethod.PUT,
        "/api/events/*/approve",
        "/api/events/*/reject",
        "/api/stalls/*/approve",
        "/api/stalls/*/reject"
    ).hasRole("ADMIN")

    .requestMatchers(
        "/api/tickets/check-in"
    ).hasAnyRole("ORGANIZER", "ADMIN")

    .anyRequest().authenticated()
)
            .addFilterBefore(
                    jwtFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
