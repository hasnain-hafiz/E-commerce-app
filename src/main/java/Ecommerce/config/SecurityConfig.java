package Ecommerce.config;

import Ecommerce.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF protection via double-submit cookie. Auth bootstrap endpoints (no
                // session/cookie exists yet when calling them) are exempted; every other
                // state-changing request must carry the X-XSRF-TOKEN header.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .ignoringRequestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/authenticate",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Public read endpoints
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/authenticate",
                                "/api/v1/auth/refresh", "/api/v1/auth/logout", "/api/v1/auth/warmup").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/product/**", "/api/v1/category/**", "/api/v1/image/download/**",
                                "/api/v1/health", "/api/v1/csrf").permitAll()
                        .requestMatchers("/api/v1/ai/**").authenticated()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Role-scoped areas (defense in depth alongside @PreAuthorize on methods)
                        .requestMatchers("/api/v1/auth/me").authenticated()
                        .requestMatchers("/api/v1/seller/**").hasRole("SELLER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/image/upload").hasRole("SELLER")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/image/update/**").hasRole("SELLER")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/image/delete/**").hasRole("SELLER")
                        .requestMatchers("/api/v1/auth/all", "/api/v1/auth/*").hasRole("ADMIN")
                        .requestMatchers("/api/v1/cart/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/order/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/category/add").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/category/update/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/category/delete/**").hasRole("ADMIN")

                        // Deny by default - anything not explicitly listed above requires auth.
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Authentication required\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"You are not authorized to perform this action\",\"data\":null}");
                        }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, RateLimitFilter.class)
                .headers(headers -> headers
                        .contentTypeOptions(withDefaults -> {})
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(encoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    /**
     * Cross-site deployment (SPA and API on different domains) means the CSRF
     * cookie needs SameSite=None + Secure=true too, matching the auth cookies -
     * otherwise the browser drops it just like it would drop a Lax cookie.
     */
    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.sameSite("None").secure(true));
        return repository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
