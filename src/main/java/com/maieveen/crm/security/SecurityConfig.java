package com.maieveen.crm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(AdminUserRepository adminUserRepository) {
        return username -> adminUserRepository.findByUsername(username)
                .map(admin -> {
                    String[] roles = admin.getRoles().stream()
                            .map(Role::getName)
                            .toArray(String[]::new);

                    UserDetails user = User.withUsername(admin.getUsername())
                            .password(admin.getPasswordHash())
                            .roles(roles)
                            .disabled(!admin.isEnabled())
                            .build();
                    return user;
                })
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/crm", "/crm/login", "/css/**", "/js/**", "/actuator/health").permitAll()
                .requestMatchers("/crm/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/crm/login")
                .defaultSuccessUrl("/crm/admin", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/crm/logout")
                .logoutSuccessUrl("/crm/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
