package edu.dosw.styleradar.pagos.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.pagos.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Autenticación del microservicio.
 *
 * <p>Prioridad 1: si {@code styleradar.security.jwt.enabled=true}, valida la
 * firma del Bearer JWT localmente (RS256 con clave pública o HS256 con secreto)
 * y toma la identidad de los claims. Prioridad 2: si no hay token, acepta las
 * cabeceras que inyecta el Gateway. El control de rol ADMIN se hace en
 * {@code SecurityConfig}.</p>
 */
public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final List<String> RUTAS_PUBLICAS = List.of(
            "/api/v1/webhooks",
            "/api/v1/health",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html");

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;
    private final JwtParser jwtParser;

    public ServiceAuthenticationFilter(JwtProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.jwtParser = construirParser(properties);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return RUTAS_PUBLICAS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest requestFinal = request;
        try {
            if (properties.isEnabled()) {
                requestFinal = autenticarConJwt(request, response);
                if (requestFinal == null) {
                    return;
                }
            }
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                autenticarConCabeceras(requestFinal);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            SecurityErrorWriter.escribir(request, response, 401, "UNAUTHORIZED",
                    "El token de autenticación es inválido o ha expirado.", objectMapper);
            return;
        }
        filterChain.doFilter(requestFinal, response);
    }

    private HttpServletRequest autenticarConJwt(HttpServletRequest request,
                                                HttpServletResponse response) throws IOException {
        String token = extraerToken(request);
        if (token == null) {
            if (properties.isRequireToken()) {
                SecurityErrorWriter.escribir(request, response, 401, "UNAUTHORIZED",
                        "Debe iniciar sesión para continuar.", objectMapper);
                return null;
            }
            return request;
        }

        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            SecurityErrorWriter.escribir(request, response, 401, "UNAUTHORIZED",
                    "El token no contiene la identidad del usuario.", objectMapper);
            return null;
        }

        String headerId = request.getHeader(HEADER_USER_ID);
        if (headerId != null && !headerId.isBlank() && !headerId.equals(subject)) {
            SecurityErrorWriter.escribir(request, response, 401, "UNAUTHORIZED",
                    "La identidad del token no coincide con la cabecera de usuario.", objectMapper);
            return null;
        }

        List<String> roles = normalizarRoles(extraerRoles(claims));
        if (roles.isEmpty()) {
            String rolHeader = request.getHeader(HEADER_USER_ROLE);
            if (rolHeader != null && !rolHeader.isBlank()) {
                roles = normalizarRoles(List.of(rolHeader));
            }
        }
        var authorities = roles.stream()
                .map(ServiceAuthenticationFilter::authority)
                .toList();
        var authentication = new UsernamePasswordAuthenticationToken(subject, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new UserHeaderRequestWrapper(request, subject,
                roles.isEmpty() ? null : roles.get(0));
    }

    private void autenticarConCabeceras(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        if (userId == null || userId.isBlank()) {
            return;
        }
        String rol = request.getHeader(HEADER_USER_ROLE);
        List<String> roles = normalizarRoles(rol == null || rol.isBlank() ? List.of() : List.of(rol));
        var authentication = new UsernamePasswordAuthenticationToken(
                userId, null, roles.stream().map(ServiceAuthenticationFilter::authority).toList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static String extraerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private static List<String> extraerRoles(Claims claims) {
        List<String> roles = new ArrayList<>();
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof Collection<?> coleccion) {
            coleccion.forEach(valor -> roles.add(String.valueOf(valor)));
        } else if (rolesClaim != null) {
            roles.add(String.valueOf(rolesClaim));
        }
        Object roleClaim = claims.get("role");
        if (roleClaim != null) {
            roles.add(String.valueOf(roleClaim));
        }
        Object authorities = claims.get("authorities");
        if (authorities instanceof Collection<?> coleccion) {
            coleccion.forEach(valor -> roles.add(String.valueOf(valor)));
        }
        return roles;
    }

    /**
     * El Gateway propaga el claim {@code roles} como {@code "[ADMIN_STYLERADAR]"}.
     * Se limpian corchetes, comillas y separadores para que Spring Security
     * reconozca el rol real del ecosistema.
     */
    private static List<String> normalizarRoles(Collection<String> roles) {
        return roles.stream()
                .filter(rol -> rol != null && !rol.isBlank())
                .flatMap(rol -> java.util.Arrays.stream(rol.split(",")))
                .map(ServiceAuthenticationFilter::limpiarRol)
                .filter(rol -> !rol.isBlank())
                .distinct()
                .toList();
    }

    private static String limpiarRol(String rol) {
        String limpio = rol.trim();
        while (limpio.startsWith("[") || limpio.startsWith("\"")) {
            limpio = limpio.substring(1).trim();
        }
        while (limpio.endsWith("]") || limpio.endsWith("\"")) {
            limpio = limpio.substring(0, limpio.length() - 1).trim();
        }
        return limpio;
    }

    private static SimpleGrantedAuthority authority(String rol) {
        String normalizado = rol.trim().toUpperCase(Locale.ROOT);
        if (!normalizado.startsWith("ROLE_")) {
            normalizado = "ROLE_" + normalizado;
        }
        return new SimpleGrantedAuthority(normalizado);
    }

    private static JwtParser construirParser(JwtProperties properties) {
        if (!properties.isEnabled()) {
            return null;
        }
        if (properties.getPublicKey() != null && !properties.getPublicKey().isBlank()) {
            return Jwts.parser().verifyWith(leerClavePublica(properties.getPublicKey())).build();
        }
        if (properties.getSecretBase64() != null && !properties.getSecretBase64().isBlank()) {
            byte[] secreto = Base64.getDecoder().decode(properties.getSecretBase64());
            return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secreto)).build();
        }
        throw new IllegalStateException(
                "JWT está habilitado pero no se configuró JWT_PUBLIC_KEY ni JWT_SECRET_BASE64.");
    }

    private static PublicKey leerClavePublica(String valor) {
        try {
            String pem = valor;
            if (!valor.contains("BEGIN PUBLIC KEY")) {
                Resource resource = new DefaultResourceLoader().getResource(valor);
                pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            String cuerpo = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(cuerpo);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible cargar la clave pública JWT.", ex);
        }
    }

    /** Agrega las cabeceras derivadas del JWT para que los controladores las lean. */
    private static final class UserHeaderRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {

        private final String userId;
        private final String role;

        private UserHeaderRequestWrapper(HttpServletRequest request, String userId, String role) {
            super(request);
            this.userId = userId;
            this.role = role;
        }

        @Override
        public String getHeader(String name) {
            if (HEADER_USER_ID.equalsIgnoreCase(name) && userId != null) {
                return userId;
            }
            if (HEADER_USER_ROLE.equalsIgnoreCase(name) && role != null) {
                return role;
            }
            return super.getHeader(name);
        }

        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            if (HEADER_USER_ID.equalsIgnoreCase(name) && userId != null) {
                return java.util.Collections.enumeration(List.of(userId));
            }
            if (HEADER_USER_ROLE.equalsIgnoreCase(name) && role != null) {
                return java.util.Collections.enumeration(List.of(role));
            }
            return super.getHeaders(name);
        }
    }
}
