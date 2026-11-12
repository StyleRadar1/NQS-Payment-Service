package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.AdminPagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultarPagosAdminUseCaseImpl implements AdminPagoPort {

    private final OrdenPagoRepositoryPort ordenPagoRepository;

    @Override
    public List<OrdenPago> consultarPagos(EstadoPago estadoFiltro) {
        log.info("Admin consulta pagos con filtro: {}", estadoFiltro);
        if (estadoFiltro == null) {
            return ordenPagoRepository.buscarTodos();
        }
        return ordenPagoRepository.buscarPorEstado(estadoFiltro);
    }
}