package com.project.fooddelivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/customer/restaurants/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/customer/menu/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/owner/**").hasRole("RESTAURANT_OWNER")
                .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/api/partner/**").hasRole("DELIVERY_PARTNER")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
            User.withUsername("admin").password(passwordEncoder.encode("admin123")).roles("ADMIN").build(),
            User.withUsername("owner1").password(passwordEncoder.encode("owner123")).roles("RESTAURANT_OWNER").build(),
            User.withUsername("owner2").password(passwordEncoder.encode("owner123")).roles("RESTAURANT_OWNER").build(),
            User.withUsername("customer1").password(passwordEncoder.encode("customer123")).roles("CUSTOMER").build(),
            User.withUsername("customer2").password(passwordEncoder.encode("customer123")).roles("CUSTOMER").build(),
            User.withUsername("partner1").password(passwordEncoder.encode("partner123")).roles("DELIVERY_PARTNER").build(),
            User.withUsername("partner2").password(passwordEncoder.encode("partner123")).roles("DELIVERY_PARTNER").build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
