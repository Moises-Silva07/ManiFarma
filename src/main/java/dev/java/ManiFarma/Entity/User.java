package dev.java.ManiFarma.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String senha;
    private boolean isClient;

    // ADICIONE ESTA LINHA
    @Column(name = "is_disabled")
    private boolean isDisabled = false; // Garante que o padrão seja 'false' (ativo)
}