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
        if (usuarioRepository.count() > 0) {
            log.info("Ya existen usuarios en la BD, se omite la inicialización");
            return;
        }

        crearUsuarioSiNoExiste("Admin", "admin@uquindio.edu.co", "quindio123", Rol.ADMINISTRADOR);
        crearUsuarioSiNoExiste("Responsable", "respo@uquindio.edu.co", "quindio123", Rol.RESPONSABLE);
        crearUsuarioSiNoExiste("Estudiante", "ruben@uquindio.edu.co", "quindio123", Rol.ESTUDIANTE);

        log.info("Usuarios iniciales creados correctamente");
    }

    private void crearUsuarioSiNoExiste(String nombre, String email, String password, Rol rol) {
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario usuario = Usuario.builder()
                    .nombre(nombre)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .rol(rol)
                    .activo(true)
                    .build();
            usuarioRepository.save(usuario);
            log.info("Usuario creado: {} / {} / {}", email, rol, password);
        }
    }
}
