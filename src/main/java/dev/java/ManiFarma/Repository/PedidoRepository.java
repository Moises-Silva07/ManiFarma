package dev.java.ManiFarma.Repository;

import dev.java.ManiFarma.Entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
//  28/08/2025
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByEmployeeId(Long employeeId);
}
