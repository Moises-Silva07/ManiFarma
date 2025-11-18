package dev.java.ManiFarma.DTO;

import lombok.Data;

@Data
public class UserRegisterRequestDTO {
    private String nome;
    private String email;
    private String senha;
    private boolean isClient;
    // Campos específicos de Cliente
    private String cpf;
    private String cep;
    private String rua;
    private String bairro;
    private String cidade;
    private String estado;
    private String telefone;
    // Campos específicos de Employee
    private String role;
    private Double salary;
    private String shift;
}