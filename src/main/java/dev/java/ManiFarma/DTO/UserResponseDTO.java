package dev.java.ManiFarma.DTO;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private boolean isClient;
    private String token;
    private boolean isDisabled;
    private boolean isAnonymized;
    private String cpf;
    private String cep;
    private String rua;
    private String bairro;
    private String cidade;
    private String estado;
    private String telefone;
    private String role;
    private Double salary;
    private String shift;
}
