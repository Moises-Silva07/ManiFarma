package dev.java.ManiFarma.DTO;

public class PedidoProdutoRequestDTO {

    private Long produtoId;
    private Double dose;
    private String unidade;
    private Integer quantidade;

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Double getDose() { return dose; }
    public void setDose(Double dose) { this.dose = dose; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}
