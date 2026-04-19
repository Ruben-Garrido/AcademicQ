package com.academiq.academiq.controller;

import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.Rol;
import com.academiq.academiq.dto.request.UsuarioRequest;
import com.academiq.academiq.dto.response.UsuarioResponse;
import com.academiq.academiq.mapper.SolicitudMapper;
import com.academiq.academiq.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private SolicitudMapper mapper;

    @Test
    @DisplayName("✅ PASA: POST /usuarios retorna 201 Created")
    void test_crear_cuando_datosValidos_deberia_retornar201() throws Exception {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Test");
        request.setEmail("test@uni.edu");
        request.setPassword("pass123");
        request.setRol(Rol.ESTUDIANTE);

        Usuario usuarioCreado = new Usuario();
        usuarioCreado.setId(UUID.randomUUID());

        UsuarioResponse response = UsuarioResponse.builder().nombre("Test").build();

        when(usuarioService.crear(any(), any(), any(), any())).thenReturn(usuarioCreado);
        when(mapper.toUsuarioResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Test"));
    }

    @Test
    @DisplayName("❌ FALLA: POST /usuarios retorna 400 si falta email")
    void test_crear_cuando_faltaEmail_deberia_retornar400() throws Exception {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Test");
        request.setPassword("pass123");
        request.setRol(Rol.ESTUDIANTE);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ PASA: GET /usuarios retorna 200 y lista")
    void test_listar_deberia_retornar200() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(new Usuario()));
        when(mapper.toUsuarioResponse(any())).thenReturn(UsuarioResponse.builder().nombre("Test").build());

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Test"));
    }
}