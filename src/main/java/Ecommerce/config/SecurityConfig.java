package Ecommerce.config;

import Ecommerce.jwt.JwtFilter;
import Ecommerce.service.Authentication.LogoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;
    private final LogoutService logoutService;
    private final RateLimitingFilter rateLimitingFilter;


    @Value("#{'${cors.allowed-origins:http://localhost:5173,http://localhost:5174,https://ecommerce-frontend-sigma-lilac.vercel.app}'.split(',')}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // NEW (Phase 3): explicit security headers. Spring Security
                // already applies sensible defaults (X-Content-Type-Options,
                // X-Frame-Options DENY) even without this block, but HSTS's
                // effect depends on being served over HTTPS (true in
                // production behind Render) and the referrer policy isn't
                // part of the defaults — both made explicit here rather
                // than relying on implicit behavior.
                .headers(headers -> headers
                        .contentTypeOptions(withDefaults())
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/category/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/image/download/**").permitAll()
                        .requestMatchers("/api/v1/image/**").authenticated()

                        .requestMatchers("/api/v1/cart/**").authenticated()
                        .requestMatchers("/api/v1/order/**").authenticated()

                        .requestMatchers("/api/v1/wishlist/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/review/**").permitAll()
                        .requestMatchers("/api/v1/review/**").authenticated()

                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        .anyRequest().permitAll())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                // NEW (Phase 3): rejects excessive login/register/forgot-password
                // attempts before they reach JwtFilter or the controller.
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                    .logoutUrl("/api/v1/auth/logout")
                    .addLogoutHandler(logoutService)
                    .logoutSuccessHandler((request, response, authentication) ->
                            SecurityContextHolder.clearContext()));


        return httpSecurity.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(encoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
