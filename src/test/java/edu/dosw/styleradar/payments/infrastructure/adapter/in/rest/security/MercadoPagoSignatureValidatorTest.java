package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
class MercadoPagoSignatureValidatorTest {

    private static final String SECRETO = "secret-webhook";

    private MercadoPagoSignatureValidator validator(String perfil) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(perfil);
        return new MercadoPagoSignatureValidator(SECRETO, environment);
    }

    private String firmar(String manifiesto) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRETO.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(manifiesto.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void aceptaFirmaValida() throws Exception {
        String manifiesto = "id:12345;request-id:req-1;ts:1700000000;";
        String firma = firmar(manifiesto);

        assertThat(validator("prod")
                .validate("ts=1700000000,v1=" + firma, "req-1", "12345")).isTrue();
    }

    @Test
    void rechazaFirmaInvalida() {
        assertThat(validator("prod")
                .validate("ts=1700000000,v1=00ff", "req-1", "12345")).isFalse();
    }

    @Test
    void rechazaCabecerasIncompletas() {
        assertThat(validator("prod").validate(null, "req-1", "12345")).isFalse();
        assertThat(validator("prod").validate("ts=1,v1=00", "req-1", null)).isFalse();
        assertThat(validator("prod").validate("solo-basura", "req-1", "12345")).isFalse();
    }

    @Test
    void aceptaDummySoloEnDevYTest() {
        assertThat(validator("dev").validate("dummy", "req-1", "12345")).isTrue();
        assertThat(validator("test").validate("dummy", "req-1", "12345")).isTrue();
        assertThat(validator("prod").validate("dummy", "req-1", "12345")).isFalse();
    }

    @Test
    void rechazaSiNoHaySecreto() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        MercadoPagoSignatureValidator sinSecreto = new MercadoPagoSignatureValidator(" ", environment);

        assertThat(sinSecreto.validate("ts=1,v1=00", "req-1", "12345")).isFalse();
    }
}
