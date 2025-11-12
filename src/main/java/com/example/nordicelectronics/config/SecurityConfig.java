package com.example.nordicelectronics.config;

import com.example.nordicelectronics.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] swaggerPaths = {
                "/",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**"
        };

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(swaggerPaths).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml", "/v3/api-docs").permitAll()
                        .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/auth/current-user", "/api/auth/users", "/api/auth/test").authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository()))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(customUserDetailsService)
                   .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
}
