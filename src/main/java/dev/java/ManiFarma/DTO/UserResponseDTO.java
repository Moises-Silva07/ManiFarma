package dev.java.ManiFarma.DTO;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private boolean isClient;
    private String token;
    private boolean isDisabled; // ADICIONE ESTA LINHA

    // (Campos de Cliente e Employee)
    private String cpf;
    private String endereco;
    private String telefone;
    private String role;
    private Double salary;
    private String shift;
}