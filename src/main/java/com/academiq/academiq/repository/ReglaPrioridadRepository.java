package com.academiq.academiq.repository;

import com.academiq.academiq.domain.entity.ReglaPrioridad;
import com.academiq.academiq.domain.enums.ImpactoAcademico;
import com.academiq.academiq.domain.enums.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository

public  interface ReglaPrioridadRepository  extends JpaRepository <ReglaPrioridad,UUID>{

    Optional<ReglaPrioridad> findByTipoAndImpactoAcademico(TipoSolicitud tipo, ImpactoAcademico impactoAcademico);

}