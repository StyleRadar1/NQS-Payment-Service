package edu.dosw.styleradar.payments.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.payments.infrastructure.config.JwtProperties;
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

public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final List<String> PUBLIC_PATHS = List.of(
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
        this.jwtParser = buildParser(properties);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest finalRequest = request;
        try {
            if (properties.isEnabled()) {
                finalRequest = authenticateWithJwt(request, response);
                if (finalRequest == null) {
                    return;
                }
            }
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateWithHeaders(finalRequest);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            SecurityErrorWriter.write(request, response, 401, "UNAUTHORIZED",
                    "El token de autenticación es inválido o ha expirado.", objectMapper);
            return;
        }
        filterChain.doFilter(finalRequest, response);
    }

    private HttpServletRequest authenticateWithJwt(HttpServletRequest request,
                                                   HttpServletResponse response) throws IOException {
        String token = extractToken(request);
        if (token == null) {
            if (properties.isRequireToken()) {
                SecurityErrorWriter.write(request, response, 401, "UNAUTHORIZED",
                        "Debes iniciar sesión para continuar.", objectMapper);
                return null;
            }
            return request;
        }

        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            SecurityErrorWriter.write(request, response, 401, "UNAUTHORIZED",
                    "El token no contiene la identidad del usuario.", objectMapper);
            return null;
        }

        String headerId = request.getHeader(HEADER_USER_ID);
        if (headerId != null && !headerId.isBlank() && !headerId.equals(subject)) {
            SecurityErrorWriter.write(request, response, 401, "UNAUTHORIZED",
                    "La identidad del token no coincide con la cabecera de usuario.", objectMapper);
            return null;
        }

        List<String> roles = normalizeRoles(extractRoles(claims));
        if (roles.isEmpty()) {
            String roleHeader = request.getHeader(HEADER_USER_ROLE);
            if (roleHeader != null && !roleHeader.isBlank()) {
                roles = normalizeRoles(List.of(roleHeader));
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

    private void authenticateWithHeaders(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        if (userId == null || userId.isBlank()) {
            return;
        }
        String role = request.getHeader(HEADER_USER_ROLE);
        List<String> roles = normalizeRoles(role == null || role.isBlank() ? List.of() : List.of(role));
        var authentication = new UsernamePasswordAuthenticationToken(
                userId, null, roles.stream().map(ServiceAuthenticationFilter::authority).toList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private static List<String> extractRoles(Claims claims) {
        List<String> roles = new ArrayList<>();
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof Collection<?> collection) {
            collection.forEach(value -> roles.add(String.valueOf(value)));
        } else if (rolesClaim != null) {
            roles.add(String.valueOf(rolesClaim));
        }
        Object roleClaim = claims.get("role");
        if (roleClaim != null) {
            roles.add(String.valueOf(roleClaim));
        }
        Object authorities = claims.get("authorities");
        if (authorities instanceof Collection<?> collection) {
            collection.forEach(value -> roles.add(String.valueOf(value)));
        }
        return roles;
    }

    private static List<String> normalizeRoles(Collection<String> roles) {
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .flatMap(role -> java.util.Arrays.stream(role.split(",")))
                .map(ServiceAuthenticationFilter::cleanRole)
                .filter(role -> !role.isBlank())
                .distinct()
                .toList();
    }

    private static String cleanRole(String role) {
        String clean = role.trim();
        while (clean.startsWith("[") || clean.startsWith("\"")) {
            clean = clean.substring(1).trim();
        }
        while (clean.endsWith("]") || clean.endsWith("\"")) {
            clean = clean.substring(0, clean.length() - 1).trim();
        }
        return clean;
    }

    private static SimpleGrantedAuthority authority(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return new SimpleGrantedAuthority(normalized);
    }

    private static JwtParser buildParser(JwtProperties properties) {
        if (!properties.isEnabled()) {
            return null;
        }
        if (properties.getPublicKey() != null && !properties.getPublicKey().isBlank()) {
            return Jwts.parser().verifyWith(readPublicKey(properties.getPublicKey())).build();
        }
        if (properties.getSecretBase64() != null && !properties.getSecretBase64().isBlank()) {
            byte[] secret = Base64.getDecoder().decode(properties.getSecretBase64());
            return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret)).build();
        }
        throw new IllegalStateException(
                "JWT is enabled but neither JWT_PUBLIC_KEY nor JWT_SECRET_BASE64 is configured.");
    }

    private static PublicKey readPublicKey(String value) {
        try {
            String pem = value;
            if (!value.contains("BEGIN PUBLIC KEY")) {
                Resource resource = new DefaultResourceLoader().getResource(value);
                pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            String body = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(body);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("The JWT public key could not be loaded.", ex);
        }
    }

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

