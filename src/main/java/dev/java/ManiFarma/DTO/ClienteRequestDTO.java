package dev.java.ManiFarma.DTO;

import lombok.Data;
// 28/08/2025
@Data
public class ClienteRequestDTO {
    private String nome;
    private String email;
    private String cpf;
    private String cep;
    private String rua;
    private String bairro;
    private String cidade;
    private String estado;
    private String telefone;
    private String senha;
}
