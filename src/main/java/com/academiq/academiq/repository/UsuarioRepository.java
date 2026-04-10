package com.academiq.academiq.repository;

import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository //le dice que esta clase habla con la BD con un manejo especial de errores
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRolAndActivoTrue(Rol rol);
    boolean existsByEmail(String email);
}
