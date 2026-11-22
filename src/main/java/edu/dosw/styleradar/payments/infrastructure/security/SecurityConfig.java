package edu.dosw.styleradar.payments.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.payments.infrastructure.config.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/webhooks/**",
            "/api/v1/health",
            "/actuator/health/**",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtProperties jwtProperties,
                                                   ObjectMapper objectMapper) throws Exception {
        ServiceAuthenticationFilter authenticationFilter =
                new ServiceAuthenticationFilter(jwtProperties, objectMapper);
        http
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR: stateless JWT API without session cookies
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("STYLERADAR_ADMIN", "ADMIN_STYLERADAR", "ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) ->
                                SecurityErrorWriter.write(request, response, 401, "UNAUTHORIZED",
                                        "Debes iniciar sesión para continuar.", objectMapper))
                        .accessDeniedHandler((request, response, exception) ->
                                SecurityErrorWriter.write(request, response, 403, "FORBIDDEN",
                                        "No tienes permisos para realizar esta operación.", objectMapper)))
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
