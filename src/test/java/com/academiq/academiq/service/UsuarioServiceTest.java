package com.academiq.academiq.service;

import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.Rol;
import com.academiq.academiq.exception.RecursoNoEncontradoException;
import com.academiq.academiq.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("✅ PASA: crear() debe crear un usuario exitosamente")
    void test_crear_cuando_datosSonValidos_deberia_retornarUsuarioCreado() {
        // Arrange
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(UUID.randomUUID());
        usuarioMock.setNombre("Test");
        usuarioMock.setEmail("test@uni.edu");
        usuarioMock.setRol(Rol.ESTUDIANTE);
        
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        // Act
        Usuario usuarioCreado = usuarioService.crear("Test", "test@uni.edu", "pass123", Rol.ESTUDIANTE);

        // Assert
        assertThat(usuarioCreado).isNotNull();
        assertThat(usuarioCreado.getEmail()).isEqualTo("test@uni.edu");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("❌ FALLA (Esperado): obtenerPorId() debe lanzar excepción si el usuario no existe")
    void test_obtenerPorId_cuando_usuarioNoExiste_deberia_lanzarExcepcion() {
        // Arrange
        UUID idInvalido = UUID.randomUUID();
        when(usuarioRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.obtenerPorId(idInvalido))
                .isInstanceOf(RuntimeException.class); // o el nombre exacto de tu excepcion
    }
}