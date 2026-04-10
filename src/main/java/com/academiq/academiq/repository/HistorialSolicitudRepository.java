package com.academiq.academiq.repository;

import com.academiq.academiq.domain.entity.HistorialSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialSolicitudRepository extends JpaRepository <HistorialSolicitud,UUID>{

    List<HistorialSolicitud> findBySolicitudIdOrderByFechaAccionAsc(UUID solicitudId);
}