package edu.dosw.styleradar.pagos.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.pagos.infrastructure.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Escribe respuestas 401/403 en el mismo formato que el resto de errores. */
public final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    public static void escribir(HttpServletRequest request,
                                HttpServletResponse response,
                                int status,
                                String error,
                                String message,
                                ObjectMapper objectMapper) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                ErrorResponse.of(status, error, message, request.getRequestURI()));
    }
}
