package dev.java.ManiFarma.DTO;

import dev.java.ManiFarma.Entity.StatusPedido;
import lombok.Data;
//  28/08/2025
@Data
public class PedidoRequestDTO {
    private String descricao;
    private StatusPedido status;
    private String receita; // opcional
    private Long clienteId; // vincula ao cliente
}
