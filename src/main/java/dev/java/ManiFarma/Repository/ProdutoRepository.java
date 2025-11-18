package dev.java.ManiFarma.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.java.ManiFarma.Entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}