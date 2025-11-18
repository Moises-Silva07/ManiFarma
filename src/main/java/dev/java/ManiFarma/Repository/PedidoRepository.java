package dev.java.ManiFarma.Repository;

import dev.java.ManiFarma.Entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import dev.java.ManiFarma.DTO.ClientReportDTO;
import dev.java.ManiFarma.DTO.EmployeeReportDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByEmployeeId(Long employeeId);

    @Query("select count(p) from Pedido p")
    long countAllOrders();

    @Query("select coalesce(sum(p.valorTotal), 0) from Pedido p")
    Double sumAllRevenue();

    @Query("select count(p) from Pedido p where p.status = :status")
    long countByStatus(@Param("status") dev.java.ManiFarma.Entity.StatusPedido status);

    @Query("select new dev.java.ManiFarma.DTO.ClientReportDTO(c.id, c.nome, c.email, count(p), coalesce(sum(p.valorTotal),0)) " +
            "from Pedido p join p.cliente c " +
            "where (:from is null or p.createdAt >= :from) and (:to is null or p.createdAt <= :to) " +
            "group by c.id, c.nome, c.email order by sum(p.valorTotal) desc")
    List<ClientReportDTO> findTopClients(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select new dev.java.ManiFarma.DTO.EmployeeReportDTO(e.id, e.nome, count(p), coalesce(sum(p.valorTotal),0)) " +
            "from Pedido p join p.employee e " +
            "where (:from is null or p.createdAt >= :from) and (:to is null or p.createdAt <= :to) " +
            "group by e.id, e.nome order by count(p) desc")
    List<EmployeeReportDTO> findTopEmployees(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select p from Pedido p where (:from is null or p.createdAt >= :from) and (:to is null or p.createdAt <= :to) order by p.createdAt desc")
    List<Pedido> findOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}