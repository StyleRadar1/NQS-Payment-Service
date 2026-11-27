package edu.dosw.styleradar.payments.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void propagatesIncomingCorrelationIdAndCleansMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/plans");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-pay-101");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcInsideChain = new AtomicReference<>();

        FilterChain chain = (req, res) -> mdcInsideChain.set(MDC.get(CorrelationIdFilter.MDC_KEY));
        filter.doFilter(request, response, chain);

        assertThat(mdcInsideChain.get()).isEqualTo("corr-pay-101");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("corr-pay-101");
        assertThat(response.getHeader(CorrelationIdFilter.REQUEST_ID_HEADER)).isEqualTo("corr-pay-101");
    }

    @Test
    void generatesCorrelationIdWhenMissingOrInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/plans");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "invalid\nvalue");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        String generated = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(generated).isNotBlank().doesNotContain("\n").doesNotContain("\r");
        assertThat(response.getHeader(CorrelationIdFilter.REQUEST_ID_HEADER)).isEqualTo(generated);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
