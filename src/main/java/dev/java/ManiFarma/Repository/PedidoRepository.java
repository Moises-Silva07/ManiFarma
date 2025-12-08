package dev.java.ManiFarma.Repository;

import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Entity.StatusPedido;
import dev.java.ManiFarma.DTO.ClientReportDTO; // Adicione este import
import dev.java.ManiFarma.DTO.EmployeeReportDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByEmployeeId(Long employeeId);

    @Query("select count(p) from Pedido p where p.createdAt BETWEEN :start AND :end")
    long countAllOrders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select coalesce(sum(p.valorTotal), 0) from Pedido p where p.createdAt BETWEEN :start AND :end")
    Double sumAllRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select count(p) from Pedido p where p.status = :status AND p.createdAt BETWEEN :start AND :end")
    long countByStatus(@Param("status") StatusPedido status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            SELECT new dev.java.ManiFarma.DTO.ClientReportDTO(
                c.id, 
                c.nome, 
                c.email, 
                COUNT(p), 
                COALESCE(SUM(p.valorTotal), 0.0)
            )
            FROM Pedido p
            JOIN p.cliente c
            WHERE p.createdAt BETWEEN :start AND :end
            GROUP BY c.id, c.nome, c.email
            ORDER BY SUM(p.valorTotal) DESC
            """)
    List<ClientReportDTO> findTopClients(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            SELECT new dev.java.ManiFarma.DTO.EmployeeReportDTO(
                e.id, 
                e.nome, 
                COUNT(p), 
                COALESCE(SUM(p.valorTotal), 0.0)
            )
            FROM Pedido p 
            JOIN p.employee e
            WHERE p.createdAt BETWEEN :start AND :end
            GROUP BY e.id, e.nome
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