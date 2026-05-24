package com.academiq.academiq.service;

import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.Rol;
import com.academiq.academiq.exception.RecursoNoEncontradoException;
import com.academiq.academiq.exception.UsuarioInactivoException;
import com.academiq.academiq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario crear(String nombre, String email,
                         String password, Rol rol) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un usuario con email: " + email);
        }
        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .email(email)
                .password(passwordEncoder.encode(password))
                .rol(rol)
                .activo(true)
                .build();
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorRol(Rol rol) {
        return usuarioRepository.findByRolAndActivoTrue(rol);
    }

    public Usuario obtenerPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado: " + id));
    }

    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado con email: " + email));
    }

    public Usuario obtenerActivo(UUID id) {
        Usuario usuario = obtenerPorId(id);
        if (!usuario.getActivo()) {
            throw new UsuarioInactivoException(
                    "El usuario " + usuario.getNombre() + " no está activo");
        }
        return usuario;
    }
}