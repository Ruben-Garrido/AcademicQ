package com.academiq.academiq.service;

import com.academiq.academiq.domain.entity.ReglaPrioridad;
import com.academiq.academiq.domain.enums.ImpactoAcademico;
import com.academiq.academiq.domain.enums.Prioridad;
import com.academiq.academiq.domain.enums.TipoSolicitud;
import com.academiq.academiq.repository.ReglaPrioridadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReglaPrioridadService {

    private final ReglaPrioridadRepository reglaPrioridadRepository;

    /**
     * Calcula la prioridad de una solicitud basado en reglas de negocio académicas.
     * <p>
     * Primero consulta la base de datos (ReglaPrioridadRepository) para reglas
     * configuradas dinámicamente. Si no encuentra una regla específica, usa
     * reglas por defecto hardcodeadas como fallback.
     * </p>
     *
     * @param tipo El tipo de solicitud (ej. CANCELACION_ASIGNATURA).
     * @param impacto El nivel de impacto académico percibido.
     * @param fechalimite Fecha proyectada de cierre o vencimiento.
     * @return La prioridad calculada a asignar.
     */
    public Prioridad calcular(TipoSolicitud tipo, ImpactoAcademico impacto, LocalDate fechalimite) {
        Prioridad desdeDb = consultarReglaEnDb(tipo, impacto);
        if (desdeDb != null) {
            return desdeDb;
        }
        return calcularPorDefecto(tipo, impacto, fechalimite);
    }

    private Prioridad consultarReglaEnDb(TipoSolicitud tipo, ImpactoAcademico impacto) {
        return reglaPrioridadRepository.findByTipoAndImpactoAcademico(tipo, impacto)
                .map(ReglaPrioridad::getPrioridadAsignada)
                .orElse(null);
    }

    private Prioridad calcularPorDefecto(TipoSolicitud tipo, ImpactoAcademico impacto, LocalDate fechalimite) {
        if (impacto == ImpactoAcademico.ALTO) {
            return Prioridad.ALTA;
        }
        if (fechalimite != null && fechalimite.isBefore(LocalDate.now().plusDays(7))) {
            return Prioridad.ALTA;
        }
        if (impacto == ImpactoAcademico.MEDIO
                && (tipo == TipoSolicitud.HOMOLOGACION || tipo == TipoSolicitud.CANCELACION_ASIGNATURA)) {
            return Prioridad.MEDIA;
        }
        return Prioridad.BAJA;
    }
}
