package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Validación de la firma HMAC-SHA256 de las notificaciones de Mercado Pago.
 *
 * <p>SR-RNF-13: cada notificación se acepta solo si su firma es válida.
 * El manifiesto que firma Mercado Pago es
 * {@code id:{data.id};request-id:{x-request-id};ts:{ts};} y llega en
 * {@code x-signature} como {@code ts=...,v1=...}. En dev/test se acepta
 * literalmente {@code dummy} para pruebas locales (ADR-08).</p>
 */
@Component
public class MercadoPagoSignatureValidator {

    private static final String ALGORITMO = "HmacSHA256";
    private static final String FIRMA_DEV = "dummy";

    private final String secreto;
    private final Environment environment;

    public MercadoPagoSignatureValidator(@Value("${mercadopago.webhook-secret}") String secreto,
                                         Environment environment) {
        this.secreto = secreto;
        this.environment = environment;
    }

    public boolean validar(String xSignature, String xRequestId, String dataId) {
        if (xSignature == null || xSignature.isBlank() || dataId == null || dataId.isBlank()) {
            return false;
        }
        if (FIRMA_DEV.equalsIgnoreCase(xSignature.trim())) {
            return environment.acceptsProfiles(Profiles.of("dev", "test"));
        }
        if (secreto == null || secreto.isBlank()) {
            return false;
        }

        String ts = null;
        String firmaRecibida = null;
        for (String parte : xSignature.split(",")) {
            String[] claveValor = parte.trim().split("=", 2);
            if (claveValor.length != 2) {
                continue;
            }
            if ("ts".equals(claveValor[0])) {
                ts = claveValor[1];
            } else if ("v1".equals(claveValor[0])) {
                firmaRecibida = claveValor[1];
            }
        }
        if (ts == null || firmaRecibida == null) {
            return false;
        }

        String manifiesto = "id:" + dataId
                + ";request-id:" + (xRequestId == null ? "" : xRequestId)
                + ";ts:" + ts + ";";
        String firmaCalculada = hmacHex(manifiesto);
        return MessageDigest.isEqual(
                firmaCalculada.getBytes(StandardCharsets.UTF_8),
                firmaRecibida.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    private String hmacHex(String manifiesto) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), ALGORITMO));
            return HexFormat.of().formatHex(mac.doFinal(manifiesto.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible calcular la firma del webhook.", ex);
        }
    }
}
