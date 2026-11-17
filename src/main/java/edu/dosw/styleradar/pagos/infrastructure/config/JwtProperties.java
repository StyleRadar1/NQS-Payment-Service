package edu.dosw.styleradar.pagos.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de validación JWT en el microservicio.
 *
 * <p>Decisión D11: el Gateway ya valida el token, pero el servicio puede
 * validarlo también con la clave pública RSA o el secreto HMAC, sin llamar
 * a ms-auth. En dev/test se desactiva y se aceptan las cabeceras
 * {@code X-User-Id} y {@code X-User-Role} del Gateway.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "styleradar.security.jwt")
public class JwtProperties {

    private boolean enabled;
    private boolean requireToken;
    private String publicKey;
    private String secretBase64;
    private String issuer;
}
