package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class MercadoPagoSignatureValidator {

    private static final String ALGORITMO = "HmacSHA256";
    private static final String FIRMA_DEV = "dummy";

    private final String secret;
    private final Environment environment;

    public MercadoPagoSignatureValidator(@Value("${mercadopago.webhook-secret}") String secret,
                                         Environment environment) {
        this.secret = secret;
        this.environment = environment;
    }

    public boolean validate(String xSignature, String xRequestId, String dataId) {
        if (xSignature == null || xSignature.isBlank() || dataId == null || dataId.isBlank()) {
            return false;
        }
        if (FIRMA_DEV.equalsIgnoreCase(xSignature.trim())) {
            return environment.acceptsProfiles(Profiles.of("dev", "test"));
        }
        if (secret == null || secret.isBlank()) {
            return false;
        }

        String ts = null;
        String receivedSignature = null;
        for (String part : xSignature.split(",")) {
            String[] keyValue = part.trim().split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }
            if ("ts".equals(keyValue[0])) {
                ts = keyValue[1];
            } else if ("v1".equals(keyValue[0])) {
                receivedSignature = keyValue[1];
            }
        }
        if (ts == null || receivedSignature == null) {
            return false;
        }

        String manifiesto = "id:" + dataId
                + ";request-id:" + (xRequestId == null ? "" : xRequestId)
                + ";ts:" + ts + ";";
        String firmaCalculada = hmacHex(manifiesto);
        return MessageDigest.isEqual(
                firmaCalculada.getBytes(StandardCharsets.UTF_8),
                receivedSignature.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    private String hmacHex(String manifiesto) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITMO));
            return HexFormat.of().formatHex(mac.doFinal(manifiesto.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible calcular la firma del webhook.", ex);
        }
    }
}
