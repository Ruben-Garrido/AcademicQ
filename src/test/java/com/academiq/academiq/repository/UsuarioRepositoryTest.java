package com.academiq.academiq.repository;

import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("✅ PASA: existsByEmail debe retornar true si el correo existe")
    void test_existsByEmail_cuando_emailExiste_deberia_retornarTrue() {
        // Arrange: Preparar datos
        Usuario usuario = new Usuario();
        usuario.setNombre("Test User");
        usuario.setEmail("test@universidad.edu");
        usuario.setPassword("12345");
        usuario.setRol(Rol.ESTUDIANTE);
        usuario.setActivo(true);
        entityManager.persistAndFlush(usuario);

        // Act: Ejecutar el método del repositorio
        boolean existe = usuarioRepository.existsByEmail("test@universidad.edu");

        // Assert: Validar resultado
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("❌ FALLA (Esperado): existsByEmail debe retornar false si el correo no existe")
    void test_existsByEmail_cuando_emailNoExiste_deberia_retornarFalse() {
        // Arrange: No guardamos nada en la BD
        
        // Act
        boolean existe = usuarioRepository.existsByEmail("noexiste@universidad.edu");

        // Assert
        assertThat(existe).isFalse();
    }
}