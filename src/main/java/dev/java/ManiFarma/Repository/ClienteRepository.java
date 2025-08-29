package dev.java.ManiFarma.Repository;

import dev.java.ManiFarma.Entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
//  28/08/2025
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmailAndSenha(String email, String senha);
    boolean existsByEmail(String email);
}
