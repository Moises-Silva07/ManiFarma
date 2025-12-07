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

    @Query(value = """
            SELECT c.id, u.nome, u.email, COUNT(p.id), COALESCE(SUM(p.valorTotal), 0.0)
            FROM Pedido p
            JOIN p.cliente c
            JOIN c.usuario u
            WHERE p.createdAt BETWEEN :start AND :end
            GROUP BY c.id, u.nome, u.email
            ORDER BY SUM(p.valorTotal) DESC
            """, nativeQuery = true)
    List<Object[]> findTopClients(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            SELECT new dev.java.ManiFarma.DTO.EmployeeReportDTO(e.id, e.nome, COUNT(p), COALESCE(SUM(p.valorTotal), 0.0))
            FROM Pedido p 
            JOIN p.employee e
            WHERE p.createdAt BETWEEN :start AND :end
            GROUP BY e.id 
            ORDER BY SUM(p.valorTotal) DESC
            """)
    List<EmployeeReportDTO> findTopEmployees(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
           SELECT p
           FROM Pedido p
           WHERE (:from IS NULL OR p.createdAt >= :from)
             AND (:to IS NULL OR p.createdAt <= :to)
           ORDER BY p.createdAt DESC
           """)
    List<Pedido> findOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}