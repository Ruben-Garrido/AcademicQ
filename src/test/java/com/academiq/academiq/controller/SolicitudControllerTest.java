package com.academiq.academiq.controller;

import com.academiq.academiq.domain.entity.Solicitud;
import com.academiq.academiq.domain.enums.*;
import com.academiq.academiq.dto.request.*;
import com.academiq.academiq.dto.response.*;
import com.academiq.academiq.mapper.SolicitudMapper;
import com.academiq.academiq.service.SolicitudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.academiq.academiq.security.JwtService;

@WebMvcTest(SolicitudController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva Spring Security para el test
class SolicitudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService; // mockbean for missing context bean

    @MockBean
    private SolicitudService solicitudService;
    
    @MockBean
    private com.academiq.academiq.service.GeminiAiService geminiAiService;

    @MockBean
    private SolicitudMapper mapper;

    // --- REGISTRAR ---
    @Test
    @DisplayName("✅ PASA: POST / registrar retorna 201 Created cuando es válido")
    void registrar_valido_retorna201() throws Exception {
        UUID solicitanteId = UUID.randomUUID();
        SolicitudRequest request = new SolicitudRequest();
        request.setTipo(TipoSolicitud.REGISTRO_ASIGNATURA);
        request.setDescripcion("Necesito registrar asignatura urgente");
        request.setCanal(CanalOrigen.SAC);

        when(solicitudService.registrar(any(), any(), any(), any())).thenReturn(new Solicitud());
        when(mapper.toResponse(any())).thenReturn(SolicitudResponse.builder().estado(EstadoSolicitud.REGISTRADA).build());

        mockMvc.perform(post("/api/v1/solicitudes")
                        .header("X-Solicitante-Id", solicitanteId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("REGISTRADA"));
    }

    @Test
    @DisplayName("❌ FALLA: POST / registrar retorna 400 si falta solicitante-id")
    void registrar_sinHeader_retorna400() throws Exception {
        mockMvc.perform(post("/api/v1/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // --- LISTAR ---
    @Test
    @DisplayName("✅ PASA: GET / lista solicitudes")
    void listar_retorna200() throws Exception {
        when(solicitudService.listar(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(new Solicitud())));
        when(mapper.toResponse(any())).thenReturn(SolicitudResponse.builder().id(UUID.randomUUID()).build());

        mockMvc.perform(get("/api/v1/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // --- OBTENER / ID ---
    @Test
    @DisplayName("✅ PASA: GET /{id} obtiene una solicitud")
    void obtener_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(solicitudService.obtenerPorId(id)).thenReturn(new Solicitud());
        when(mapper.toResponse(any())).thenReturn(SolicitudResponse.builder().id(id).build());

        mockMvc.perform(get("/api/v1/solicitudes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    // --- CLASIFICAR ---
    @Test
    @DisplayName("✅ PASA: PATCH /clasificar funciona válido")
    void clasificar_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        ClasificarRequest request = new ClasificarRequest();
        request.setTipo(TipoSolicitud.REGISTRO_ASIGNATURA);
        request.setImpactoAcademico(ImpactoAcademico.ALTO);
        request.setFechaLimite(LocalDate.now());
        request.setJustificacion("Justificación");

        when(solicitudService.clasificar(any(), any(), any(), any(), any(), any())).thenReturn(new Solicitud());
        when(mapper.toResponse(any())).thenReturn(SolicitudResponse.builder().estado(EstadoSolicitud.CLASIFICADA).build());

        mockMvc.perform(patch("/api/v1/solicitudes/" + id + "/clasificar")
                        .header("X-Usuario-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CLASIFICADA"));
    }

    // --- ASIGNAR ---
    @Test
    @DisplayName("✅ PASA: PATCH /asignar funciona válido")
    void asignar_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        AsignarRequest request = new AsignarRequest();
        request.setResponsableId(UUID.randomUUID());

        when(solicitudService.asignarResponsable(any(), any())).thenReturn(new Solicitud());
        when(mapper.toResponse(any())).thenReturn(SolicitudResponse.builder().estado(EstadoSolicitud.EN_ATENCION).build());

        mockMvc.perform(patch("/api/v1/solicitudes/" + id + "/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_ATENCION"));
    }

    // --- ATENDER ---
    @Test
    @DisplayName("✅ PASA: PATCH /atender funciona válido")
    void atender_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        AtenderRequest request = new AtenderRequest();
        request.setObservacion("Solucionado el tema");

        when(solicitudService.atender(any(), any(), any())).thenReturn(new Solicitud());
        when(mapper.toResponse(any())).thenReturn(SolicitudResponse.builder().estado(EstadoSolicitud.ATENDIDA).build());

        mockMvc.perform(patch("/api/v1/solicitudes/" + id + "/atender")
                        .header("X-Usuario-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));
    }

    // --- CERRAR ---
    @Test
    @DisplayName("✅ PASA: PATCH /cerrar funciona válido")
    void cerrar_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        CerrarRequest request = new CerrarRequest();
        request.setObservacionCierre("Terminado");

        when(solicitudService.cerrar(any(), any(), any())).thenReturn(new Solicitud());
        when(mapper.toResponse(any())).thenReturn(SolicitudResponse.builder().estado(EstadoSolicitud.CERRADA).build());

        mockMvc.perform(patch("/api/v1/solicitudes/" + id + "/cerrar")
                        .header("X-Usuario-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADA"));
    }

    // --- HISTORIAL ---
    @Test
    @DisplayName("✅ PASA: GET /historial retorna historial")
    void historial_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(solicitudService.obtenerHistorial(id)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/solicitudes/" + id + "/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}