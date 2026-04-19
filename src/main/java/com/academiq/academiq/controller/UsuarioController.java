package com.academiq.academiq.controller;

import com.academiq.academiq.dto.request.UsuarioRequest;
import com.academiq.academiq.dto.response.UsuarioResponse;
import com.academiq.academiq.mapper.SolicitudMapper;
import com.academiq.academiq.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SolicitudMapper mapper;

    // POST /usuarios
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toUsuarioResponse(
                        usuarioService.crear(
                                request.getNombre(),
                                request.getEmail(),
                                request.getPassword(),
                                request.getRol())));
    }

    // GET /usuarios
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(
                usuarioService.listar()
                        .stream()
                        .map(mapper::toUsuarioResponse)
                        .toList());
    }
}