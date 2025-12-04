package dev.java.ManiFarma.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidade que representa um cliente, estendendo User.
 * Os dados específicos do cliente ficam na tabela 'clientes'.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "id") // Define que a coluna 'id' é a chave estrangeira para a tabela 'users'
public class Cliente extends User {

    @Column(name = "cpf") // Mapeamento explícito das colunas
    private String cpf;

    @Column(name = "cep")
    private String cep;

    @Column(name = "rua")
    private String rua;

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "cidade")
    private String cidade;

    @Column(name = "estado")
    private String estado;

    @Column(name = "telefone")
    private String telefone;
}
