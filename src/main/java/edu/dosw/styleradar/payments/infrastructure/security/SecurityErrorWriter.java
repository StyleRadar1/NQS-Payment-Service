package edu.dosw.styleradar.payments.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

public final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    public static void write(HttpServletRequest request,
                             HttpServletResponse response,
                             int status,
                             String code,
                             String message,
                             ObjectMapper objectMapper) throws IOException {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                httpStatus == null ? HttpStatus.UNAUTHORIZED : httpStatus, message);
        problem.setTitle(httpStatus == null ? "Unauthorized" : httpStatus.getReasonPhrase());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", LocalDateTime.now().toString());
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
