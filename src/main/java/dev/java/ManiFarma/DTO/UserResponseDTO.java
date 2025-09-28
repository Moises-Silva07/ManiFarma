package dev.java.ManiFarma.DTO;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private boolean isClient;
    private String token; // <--- Certifique-se de que esta linha está presente
    // Campos específicos de Cliente
    private String cpf;
    private String endereco;
    private String telefone;
    // Campos específicos de Employee
    private String role;
    private Double salary;
    private String shift;
}
