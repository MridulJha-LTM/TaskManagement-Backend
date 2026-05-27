package springboot.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Mandatory for testing POST/PUT APIs via Postman/Angular)
                .csrf(csrf -> csrf.disable())

                // 2. Configure URL Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll() // Let browser view DB without login
                        .anyRequest().authenticated()                  // Secure all API endpoints
                )

                // 3. Enable standard Basic Authentication over HTTP network channels
                .httpBasic(Customizer.withDefaults());

        // Required to let H2 console load inside HTML frames smoothly
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    // 4. Define a fixed, static test credential set for your local environment
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails globalUser = User.withDefaultPasswordEncoder()
                .username("equifax_dev")
                .password("password123")
                .build();

        return new InMemoryUserDetailsManager(globalUser);
    }
}
