package dev.java.ManiFarma.DTO;

public class PedidoProdutoResponseDTO {

    private Long produtoId;
    private String produtoNome;
    private Integer quantidade;
    private Double dose;
    private String unidade;

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public String getProdutoNome() { return produtoNome; }
    public void setProdutoNome(String produtoNome) { this.produtoNome = produtoNome; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Double getDose() { return dose; }
    public void setDose(Double dose) { this.dose = dose; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
}
