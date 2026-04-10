package com.academiq.academiq.service;

import com.academiq.academiq.domain.enums.ImpactoAcademico;
import com.academiq.academiq.domain.enums.Prioridad;
import com.academiq.academiq.domain.enums.TipoSolicitud;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReglaPrioridadService {
    public Prioridad calcular(TipoSolicitud tipo, ImpactoAcademico impacto, LocalDate fechalimite){
        if (impacto == ImpactoAcademico.ALTO){
            return  Prioridad.ALTA;
        }
        if (fechalimite != null && fechalimite.isBefore(LocalDate.now().plusDays(7))){
            return Prioridad.ALTA;
        }
        if ( impacto == ImpactoAcademico.MEDIO && (tipo == TipoSolicitud.HOMOLOGACION || tipo == TipoSolicitud.CANCELACION_ASIGNATURA)){
            return  Prioridad.MEDIA;
        }
        return Prioridad.BAJA;
    }
}
