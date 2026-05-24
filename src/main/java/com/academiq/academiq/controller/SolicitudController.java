package com.academiq.academiq.controller;

import com.academiq.academiq.dto.request.*;
import com.academiq.academiq.dto.response.*;
import com.academiq.academiq.mapper.SolicitudMapper;
import com.academiq.academiq.service.GeminiAiService;
import com.academiq.academiq.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.academiq.academiq.domain.enums.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final GeminiAiService geminiAiService;
    private final SolicitudMapper mapper;

    // POST /solicitudes
    @PostMapping
    public ResponseEntity<SolicitudResponse> registrar(
            @Valid @RequestBody SolicitudRequest request,
            @RequestHeader("X-Solicitante-Id") UUID solicitanteId) {
        log.info("POST /solicitudes - tipo: {}, canal: {}, solicitante: {}",
                request.getTipo(), request.getCanal(), solicitanteId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(
                        solicitudService.registrar(
                                request.getTipo(),
                                request.getDescripcion(),
                                request.getCanal(),
                                solicitanteId)));
    }

    // GET /solicitudes
    @GetMapping
    public ResponseEntity<Page<SolicitudResponse>> listar(
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) TipoSolicitud tipo,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) UUID responsableId,
            Pageable pageable) {
        return ResponseEntity.ok(
                solicitudService.listar(estado, tipo, prioridad,
                                responsableId, pageable)
                        .map(mapper::toResponse));
    }

    // GET /solicitudes/simular-prioridad
    @GetMapping("/simular-prioridad")
    public ResponseEntity<PrioridadResponse> simularPrioridad(
            @RequestParam TipoSolicitud tipo,
            @RequestParam ImpactoAcademico impacto,
            @RequestParam LocalDate fechaLimite) {
        Prioridad prioridad = solicitudService.simularPrioridad(tipo, impacto, fechaLimite);
        String mensaje = String.format("El sistema asignará prioridad %s automáticamente.", prioridad);
        return ResponseEntity.ok(new PrioridadResponse(prioridad, mensaje));
    }

    // GET /solicitudes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                mapper.toResponse(solicitudService.obtenerPorId(id)));
    }

    // PATCH /solicitudes/{id}/clasificar
    @PatchMapping("/{id}/clasificar")
    public ResponseEntity<SolicitudResponse> clasificar(
            @PathVariable UUID id,
            @Valid @RequestBody ClasificarRequest request,
            @RequestHeader("X-Usuario-Id") UUID usuarioId) {
        return ResponseEntity.ok(
                mapper.toResponse(
                        solicitudService.clasificar(
                                id,
                                request.getTipo(),
                                request.getImpactoAcademico(),
                                request.getFechaLimite(),
                                request.getJustificacion(),
                                usuarioId)));
    }

    // PATCH /solicitudes/{id}/asignar
    @PatchMapping("/{id}/asignar")
    public ResponseEntity<SolicitudResponse> asignar(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarRequest request) {
        return ResponseEntity.ok(
                mapper.toResponse(
                        solicitudService.asignarResponsable(
                                id, request.getResponsableId())));
    }

    // PATCH /solicitudes/{id}/atender
    @PatchMapping("/{id}/atender")
    public ResponseEntity<SolicitudResponse> atender(
            @PathVariable UUID id,
            @Valid @RequestBody AtenderRequest request,
            @RequestHeader("X-Usuario-Id") UUID usuarioId) {
        return ResponseEntity.ok(
                mapper.toResponse(
                        solicitudService.atender(
                                id, request.getObservacion(), usuarioId)));
    }

    // PATCH /solicitudes/{id}/cerrar
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<SolicitudResponse> cerrar(
            @PathVariable UUID id,
            @Valid @RequestBody CerrarRequest request,
            @RequestHeader("X-Usuario-Id") UUID usuarioId) {
        return ResponseEntity.ok(
                mapper.toResponse(
                        solicitudService.cerrar(
                                id, request.getObservacionCierre(), usuarioId)));
    }

    // GET /solicitudes/{id}/historial
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialResponse>> historial(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                solicitudService.obtenerHistorial(id)
                        .stream()
                        .map(mapper::toHistorialResponse)
                        .toList());
    }

    // GET /solicitudes/{id}/resumen-ai
    @GetMapping("/{id}/resumen-ai")
    @PreAuthorize("hasRole('COORDINADOR') or hasRole('FUNCIONARIO')")
    public ResponseEntity<AiResumenResponse> resumirConAi(
            @PathVariable UUID id) {
        
        var solicitud = solicitudService.obtenerPorId(id);
        var historial = solicitudService.obtenerHistorial(id);
        
        String resumen = geminiAiService.generarResumen(solicitud, historial);
        
        return ResponseEntity.ok(AiResumenResponse.builder()
                .resumen(resumen)
                .modelo("gemini-1.5-flash")
                .advertencia("Este resumen es generado por IA. Verifique siempre los documentos originales.")
                .build());
    }
}