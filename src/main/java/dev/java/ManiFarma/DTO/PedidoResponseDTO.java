package dev.java.ManiFarma.DTO;

import dev.java.ManiFarma.Entity.StatusPedido;
import lombok.Data;
import java.util.List;

//  28/08/2025
@Data
public class PedidoResponseDTO {
    private Long id;
    private String descricao;
    private StatusPedido status;
    private String receita;
    private Long clienteId;
    private String clienteNome;
    private String clienteTelefone;
    private Long employeeId;
    private String employeeNome;
    private Double valorTotal;
    private List<PedidoProdutoResponseDTO> itens;

    private String linkPagamento;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public String getReceita() {
        return receita;
    }

    public void setReceita(String receita) {
        this.receita = receita;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public List<PedidoProdutoResponseDTO> getItens() {
        return itens;
    }

    public void setItens(List<PedidoProdutoResponseDTO> itens) {
        this.itens = itens;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getClienteTelefone() {
        return clienteTelefone;
    }

    public void setClienteTelefone(String clienteTelefone) {
        this.clienteTelefone = clienteTelefone;
    }

    public String getEmployeeNome() {
        return employeeNome;
    }

    public void setEmployeeNome(String employeeNome) {
        this.employeeNome = employeeNome;
    }

    public String getLinkPagamento() { return linkPagamento; }
    public void setLinkPagamento(String linkPagamento) { this.linkPagamento = linkPagamento; }
}
