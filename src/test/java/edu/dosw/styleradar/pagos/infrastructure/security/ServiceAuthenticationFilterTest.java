package edu.dosw.styleradar.pagos.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.pagos.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ServiceAuthenticationFilter (D11, ADR-08)")
class ServiceAuthenticationFilterTest {

    private static final byte[] SECRETO = "clave-de-prueba-suficientemente-larga-123".getBytes(StandardCharsets.UTF_8);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private JwtProperties propiedadesJwt() {
        JwtProperties properties = new JwtProperties();
        properties.setEnabled(true);
        properties.setRequireToken(true);
        properties.setSecretBase64(Base64.getEncoder().encodeToString(SECRETO));
        return properties;
    }

    private String token(String subject, String rol) {
        SecretKey key = Keys.hmacShaKeyFor(SECRETO);
        return Jwts.builder()
                .subject(subject)
                .claim("roles", List.of(rol))
                .signWith(key)
                .compact();
    }

    @Test
    void autenticaConJwtValido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pagos/historial");
        request.addHeader("Authorization", "Bearer " + token("USER-1", "ADMIN"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new ServiceAuthenticationFilter(propiedadesJwt(), MAPPER).doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("USER-1");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(Object::toString).contains("ROLE_ADMIN");
        HttpServletRequest envuelta = (HttpServletRequest) chain.getRequest();
        assertThat(envuelta.getHeader("X-User-Id")).isEqualTo("USER-1");
        assertThat(envuelta.getHeader("X-User-Role")).isEqualTo("ADMIN");
    }

    @Test
    void rechazaTokenInvalido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pagos/historial");
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ServiceAuthenticationFilter(propiedadesJwt(), MAPPER)
                .doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
    }

    @Test
    void exigeTokenCuandoRequireTokenEstaActivo() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pagos/historial");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ServiceAuthenticationFilter(propiedadesJwt(), MAPPER)
                .doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void rechazaIdentidadDeCabeceraDistintaAlToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pagos/historial");
        request.addHeader("Authorization", "Bearer " + token("USER-1", "USUARIO"));
        request.addHeader("X-User-Id", "USER-2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ServiceAuthenticationFilter(propiedadesJwt(), MAPPER)
                .doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void usaCabecerasCuandoJwtEstaDeshabilitado() throws Exception {
        JwtProperties properties = new JwtProperties();
        properties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pagos/historial");
        request.addHeader("X-User-Id", "USER-9");
        request.addHeader("X-User-Role", "USUARIO");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new ServiceAuthenticationFilter(properties, MAPPER).doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("USER-9");
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void normalizaRolConCorchetesDelGateway() throws Exception {
        JwtProperties properties = new JwtProperties();
        properties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/pagos");
        request.addHeader("X-User-Id", "ADMIN-1");
        request.addHeader("X-User-Role", "[ADMIN_STYLERADAR]");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ServiceAuthenticationFilter(properties, MAPPER)
                .doFilter(request, response, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(Object::toString)
                .contains("ROLE_ADMIN_STYLERADAR");
    }

    @Test
    void noFiltraRutasPublicas() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new ServiceAuthenticationFilter(propiedadesJwt(), MAPPER).doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void fallaAlConstruirSiJwtEstaActivoSinClaves() {
        JwtProperties properties = new JwtProperties();
        properties.setEnabled(true);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> new ServiceAuthenticationFilter(properties, MAPPER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_PUBLIC_KEY");
    }
}
