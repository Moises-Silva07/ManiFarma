package dev.java.ManiFarma.DTO;

import dev.java.ManiFarma.Entity.StatusPedido;
import lombok.Data;
//  28/08/2025
@Data
public class PedidoResponseDTO {
    private Long id;
    private String descricao;
    private StatusPedido status;
    private String receita;
    private Long clienteId;
    private String clienteNome;
}
