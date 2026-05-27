package com.academiq.academiq.config;

import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.Rol;
import com.academiq.academiq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearOActualizarUsuario("Juan Fernando", "admin@uquindio.edu.co", "quindio123", Rol.ADMINISTRADOR);
        crearOActualizarUsuario("Laura Garcia", "respo@uquindio.edu.co", "quindio123", Rol.RESPONSABLE);
        crearOActualizarUsuario("Ruben Garrido", "ruben@uquindio.edu.co", "quindio123", Rol.ESTUDIANTE);
    }

    private void crearOActualizarUsuario(String nombre, String email, String password, Rol rol) {
        var optional = usuarioRepository.findByEmail(email);
        if (optional.isEmpty()) {
            Usuario usuario = Usuario.builder()
                    .nombre(nombre)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .rol(rol)
                    .activo(true)
                    .build();
            usuarioRepository.save(usuario);
            log.info("Usuario creado: {} / {} / {}", email, rol, nombre);
        } else {
            Usuario usuario = optional.get();
            usuario.setNombre(nombre);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setRol(rol);
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
            log.info("Usuario actualizado: {} / {} / {}", email, rol, nombre);
        }
    }
}
