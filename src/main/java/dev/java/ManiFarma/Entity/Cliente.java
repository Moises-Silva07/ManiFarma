package dev.java.ManiFarma.Entity;

import jakarta.persistence.*;
import lombok.Data;
//  28/08/2025
@Data
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String cpf;
    private String endereco;
    private String telefone;
    private String senha; // senha em texto por enquanto
}
