package dev.java.ManiFarma.Repository;

import dev.java.ManiFarma.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndSenha(String email, String senha);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email); // Adicione esta linha
}