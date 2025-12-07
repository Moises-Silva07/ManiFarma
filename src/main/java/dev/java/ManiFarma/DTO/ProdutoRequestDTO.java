package dev.java.ManiFarma.DTO;

public class ProdutoRequestDTO {
    private String nome;
    private Double preco;
    private String unidade;

    public ProdutoRequestDTO() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
}
