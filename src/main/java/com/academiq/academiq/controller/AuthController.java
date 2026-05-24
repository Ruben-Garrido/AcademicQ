package com.academiq.academiq.controller;

import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.dto.request.AuthRequest;
import com.academiq.academiq.dto.response.AuthResponse;
import com.academiq.academiq.security.JwtService;
import com.academiq.academiq.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        
        // Autenticar credenciales
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Si pasa, obtener el usuario
        Usuario usuario = usuarioService.obtenerPorEmail(request.getEmail());

        // Generar Token JWT
        String jwtToken = jwtService.generateToken(usuario);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build());
    }
}
