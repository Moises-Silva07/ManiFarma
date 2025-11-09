package dev.java.ManiFarma.Entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double preco;

    @OneToMany(mappedBy = "produto")
    private List<PedidoProduto> pedidos = new ArrayList<>();

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public List<PedidoProduto> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<PedidoProduto> pedidos) {
        this.pedidos = pedidos;
    }
}
