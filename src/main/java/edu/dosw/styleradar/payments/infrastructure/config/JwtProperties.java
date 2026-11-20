package edu.dosw.styleradar.payments.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
