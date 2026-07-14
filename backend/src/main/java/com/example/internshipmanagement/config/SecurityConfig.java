package com.example.internshipmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final LoggingFilter loggingFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/auth/me").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/auth/change-password").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/students").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/students/{id}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/students/{id}").hasAnyRole("ADMIN", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/api/students/{id}").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/students", "/api/students/**").hasAnyRole("ADMIN", "MENTOR")
                    .requestMatchers(HttpMethod.GET, "/api/mentors/{id}").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/api/mentors").hasAnyRole("ADMIN", "STUDENT")
                    .requestMatchers(HttpMethod.POST, "/api/mentors").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/mentors/{id}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/mentors/{id}").hasAnyRole("ADMIN", "MENTOR")
                    .requestMatchers(HttpMethod.GET, "/api/internship-phases", "/api/internship-phases/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/internship-phases", "/api/internship-phases/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/evaluation-criteria", "/api/evaluation-criteria/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/evaluation-criteria", "/api/evaluation-criteria/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/assessment-rounds", "/api/assessment-rounds/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/assessment-rounds","/api/assessment-rounds/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/round-criteria", "/api/round-criteria/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers("/api/round-criteria", "/api/round-criteria/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/internship-assignments", "/api/internship-assignments/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers(HttpMethod.DELETE, "/api/internship-assignments/{id}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/internship-assignments").hasAnyRole("ADMIN", "MENTOR")
                    .requestMatchers(HttpMethod.PUT, "/api/internship-assignments/**").hasAnyRole("ADMIN", "MENTOR")
                    .requestMatchers("/api/internship-assignments", "/api/internship-assignments/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/assessment-results", "/api/assessment-results/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                    .requestMatchers(HttpMethod.DELETE, "/api/assessment-results/{id}").hasAnyRole("ADMIN", "MENTOR")
                    .requestMatchers("/api/assessment-results", "/api/assessment-results/**").hasRole("MENTOR")
                    // Job Search - only students can search
                    .requestMatchers("/api/jobs/**").hasRole("STUDENT")
                    // CV Review - only students can review
                    .requestMatchers("/api/cv/**").hasRole("STUDENT")
                    // Frontend SPA — static assets & client-side routes (public)
                    .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                    .requestMatchers("/assets/**", "/static/**").permitAll()
                    .requestMatchers("/*.js", "/*.css", "/*.png", "/*.svg", "/*.ico", "/*.json").permitAll()
                    // Any other non-API path is an SPA route — permit so React Router handles it
                    .requestMatchers(request -> !request.getRequestURI().startsWith("/api/")).permitAll()
                    // Everything else (including unknown /api/ paths) requires auth
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .headers(headers -> headers
                .xssProtection(HeadersConfigurer.XXssConfig::disable) // deprecated; use CSP instead
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            )
            .addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Gemini-Api-Key", "X-OpenAI-Api-Key"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
