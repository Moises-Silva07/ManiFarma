package dev.java.ManiFarma.DTO;

import lombok.Data;

@Data
public class ClienteResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String endereco;
    private String telefone;
}
