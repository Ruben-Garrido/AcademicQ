package com.academiq.academiq.repository;

import com.academiq.academiq.domain.entity.Solicitud;
import com.academiq.academiq.domain.enums.EstadoSolicitud;
import com.academiq.academiq.domain.enums.Prioridad;
import com.academiq.academiq.domain.enums.TipoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SolicitudRepository extends JpaRepository <Solicitud,UUID>{
    Page<Solicitud> findByEstado(EstadoSolicitud estado, Pageable pageable);
    Page<Solicitud> findByTipo(TipoSolicitud tipo, Pageable pageable);
    Page<Solicitud> findByPrioridad(Prioridad prioridad, Pageable pageable);
    Page<Solicitud> findByResponsableId(UUID responsableId, Pageable pageable);
    List<Solicitud> findBySolicitanteId(UUID solicitanteId);
}