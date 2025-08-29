package dev.java.ManiFarma.DTO;

import lombok.Data;

@Data
public class ClienteRequestDTO {
    private String nome;
    private String email;
    private String cpf;
    private String endereco;
    private String telefone;
    private String senha;
}
