package com.khesam.cashcard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * {@code @Configuration} annotation tells Spring to use this class to configure Spring and Spring Boot itself
 * <p>
 * In order to avoid "leaking" information about our application,
 * Spring Security has configured Spring Web to return a generic 403 FORBIDDEN in most error conditions.
 * If almost everything results in a 403 FORBIDDEN response then an attacker doesn't really know what's going on.
 */
@Configuration
public class SecurityConfig {

    /**
     * @return Spring Security expects a Bean to configure its Filter Chain
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //All HTTP requests to cashcards/ endpoints are required
        // to be authenticated using HTTP Basic Authentication security (username and password).
        http.authorizeHttpRequests(request ->
                        request.requestMatchers("/cashcards/**")
                                .hasRole("CARD-OWNER")
                ).csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure a user named sarah1 with password abc123.
     * @return Spring's IoC container will find the UserDetailsService Bean and Spring Data will use it when needed.
     */
    @Bean
    UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails sarah = users
                .username("sarah1")
                .password(passwordEncoder.encode("abc123"))
                .roles("CARD-OWNER") // No roles for now
                .build();

        UserDetails hankOwnsNoCards = users
                .username("hank-owns-no-cards")
                .password(passwordEncoder.encode("qrs456"))
                .roles("NON-OWNER") // new role
                .build();

        UserDetails kumar = users
                .username("kumar2")
                .password(passwordEncoder.encode("xyz789"))
                .roles("CARD-OWNER")
                .build();

        return new InMemoryUserDetailsManager(sarah, hankOwnsNoCards, kumar);
    }
}
