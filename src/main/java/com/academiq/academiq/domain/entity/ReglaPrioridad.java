package com.academiq.academiq.domain.entity;

import com.academiq.academiq.domain.enums.ImpactoAcademico;
import com.academiq.academiq.domain.enums.Prioridad;
import com.academiq.academiq.domain.enums.TipoSolicitud;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reglas_prioridad")
@Data // genera los getter y setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReglaPrioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSolicitud tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImpactoAcademico impactoAcademico;

    @Column(nullable = false)
    private LocalDate fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridad prioridadAsignada;
}