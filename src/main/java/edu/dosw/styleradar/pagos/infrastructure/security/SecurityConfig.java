package edu.dosw.styleradar.pagos.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.pagos.infrastructure.config.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad perimetral del microservicio.
 *
 * <p>El Gateway valida el JWT y propaga identidad; este servicio puede
 * revalidar el token (D11) y, en cualquier caso, exige autenticación y el rol
 * ADMIN para {@code /api/v1/admin/**} (SR-RF-75/98). El webhook y la salud
 * quedan públicos; el webhook se protege con HMAC (SR-RNF-13).</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] RUTAS_PUBLICAS = {
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
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR: API stateless con JWT; no hay cookies de sesión que proteger
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "ADMIN_STYLERADAR")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) ->
                                SecurityErrorWriter.escribir(request, response, 401, "UNAUTHORIZED",
                                        "Debe iniciar sesión para continuar.", objectMapper))
                        .accessDeniedHandler((request, response, exception) ->
                                SecurityErrorWriter.escribir(request, response, 403, "FORBIDDEN",
                                        "No tiene permisos para realizar esta operación.", objectMapper)))
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
