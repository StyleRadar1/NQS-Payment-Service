package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.application.port.in.AdminPagoPort;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SR-RF-75: el administrador de StyleRadar consulta todos los pagos,
 * con filtro opcional por estado. El control de rol se hace en el controlador
 * y en la configuración de seguridad.
 */
@Service
public class AdminPagoUseCase implements AdminPagoPort {

    private final OrdenPagoRepositoryPort ordenPagoRepository;

    public AdminPagoUseCase(OrdenPagoRepositoryPort ordenPagoRepository) {
        this.ordenPagoRepository = ordenPagoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listar(EstadoPago estado) {
        return ordenPagoRepository.listar(estado).stream()
                .map(PagoResponse::desde)
                .toList();
    }
}
