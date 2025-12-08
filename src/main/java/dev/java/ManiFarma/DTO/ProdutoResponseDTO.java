package dev.java.ManiFarma.DTO;

public class ProdutoResponseDTO {
    private Long id;
    private String nome;
    private Double precoPorUnidade;
    private String unidade;

    public ProdutoResponseDTO() {}

    public ProdutoResponseDTO(Long id, String nome, Double precoPorUnidade, String unidade) {
        this.id = id;
        this.nome = nome;
        this.precoPorUnidade = precoPorUnidade;
        this.unidade = unidade;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Double getPrecoPorUnidade() { return precoPorUnidade; }
    public String getUnidade() { return unidade; }

    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setPrecoPorUnidade(Double precoPorUnidade) { this.precoPorUnidade = precoPorUnidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public double getPreco() {
        return 0;
    }
}
