package com.leilao.leilaoapp.config;

import com.leilao.leilaoapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http
                .authorizeHttpRequests(auth -> auth
                                               .requestMatchers(
                                                       "/", "/login", "/register",
                                                       "/items/**", "/css/**", "/js/**",
                                                       "/uploads/**"
                                                               ).permitAll()
                                               .requestMatchers("/ws/**").permitAll()
                                               .requestMatchers("/admin/**").hasRole("ADMIN")
                                               .requestMatchers("/pagamento/**").authenticated()
                                               .requestMatchers("/minha-conta/**").authenticated()
                                               .anyRequest().authenticated()
                                      )
                .formLogin(form -> form
                                   .loginPage("/login")
                                   .defaultSuccessUrl("/", true)
                                   .permitAll()
                          )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                       )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**")
                     );
        
        return http.build();
    }
}