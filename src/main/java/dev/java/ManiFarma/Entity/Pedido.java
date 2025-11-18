package dev.java.ManiFarma.Entity;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    private String receita; // nome do arquivo (ex: receita_123.pdf)

    @Column(length = 255)
    private String linkPagamento; // link de pagamento gerado

    private Double valorTotal;

    // 🔹 Novo campo — caminho completo do arquivo no servidor
    @Column(length = 500)
    private String caminhoReceita;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoProduto> itens = new ArrayList<>();

    @Column(name = "receita_url")
    private String receitaUrl; // caso queira expor via link público

    // ===============================
    // Getters e Setters
    // ===============================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }

    public String getReceita() { return receita; }
    public void setReceita(String receita) { this.receita = receita; }

    public String getLinkPagamento() { return linkPagamento; }
    public void setLinkPagamento(String linkPagamento) { this.linkPagamento = linkPagamento; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public List<PedidoProduto> getItens() { return itens; }
    public void setItens(List<PedidoProduto> itens) { this.itens = itens; }

    public String getReceitaUrl() { return receitaUrl; }
    public void setReceitaUrl(String receitaUrl) { this.receitaUrl = receitaUrl; }

    public String getCaminhoReceita() { return caminhoReceita; }
    public void setCaminhoReceita(String caminhoReceita) { this.caminhoReceita = caminhoReceita; }
}
