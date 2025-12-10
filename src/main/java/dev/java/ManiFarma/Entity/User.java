package dev.java.ManiFarma.Entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED) // Correto para sua estrutura de DB
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "is_client", nullable = false)
    private boolean isClient;

    @Column(name = "is_disabled", nullable = false)
    private boolean isDisabled = false;

    // --- CAMPO ADICIONADO PARA LGPD ---
    @Column(name = "is_anonymized", nullable = false)
    private boolean isAnonymized = false;
}
