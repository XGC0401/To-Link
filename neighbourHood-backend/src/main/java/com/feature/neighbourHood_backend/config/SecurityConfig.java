package com.feature.neighbourHood_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.feature.neighbourHood_backend.filter.jwtAuthFilter;
import com.feature.neighbourHood_backend.service.UserService;
import com.feature.neighbourHood_backend.util.jwtUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final jwtUtil jwtUtil;
    private final UserService userService;

    public SecurityConfig(jwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/register").permitAll()
                        .requestMatchers("/db-test").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new jwtAuthFilter(jwtUtil, userService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
