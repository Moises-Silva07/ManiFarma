package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.ClientReportDTO;
import dev.java.ManiFarma.DTO.EmployeeReportDTO;
import dev.java.ManiFarma.DTO.ReportSummaryDTO;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Entity.StatusPedido;
import dev.java.ManiFarma.Repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReportService {

    private final PedidoRepository pedidoRepository;

    public ReportService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Gera um resumo com estatísticas gerais dos pedidos:
     * total de pedidos, receita total e contagem por status.
     */
    public ReportSummaryDTO getSummary() {
        long totalPedidos = pedidoRepository.countAllOrders();
        Double receitaTotal = pedidoRepository.sumAllRevenue();
        if (receitaTotal == null) receitaTotal = 0.0;

        long pendentes = pedidoRepository.countByStatus(StatusPedido.PENDENTE);
        long pagos = pedidoRepository.countByStatus(StatusPedido.PAGO);
        long concluidos = pedidoRepository.countByStatus(StatusPedido.CONCLUIDO);
        long cancelados = pedidoRepository.countByStatus(StatusPedido.CANCELADO);

        return new ReportSummaryDTO(totalPedidos, receitaTotal, pendentes, pagos, concluidos, cancelados);

    }

    /**
     * Retorna todos os pedidos dentro de um intervalo de datas.
     */
    public List<Pedido> getOrdersBetween(LocalDate from, LocalDate to) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : null;
        return pedidoRepository.findOrdersBetween(start, end);
    }

    /**
     * Retorna os clientes com maior volume de pedidos ou receita.
     */
    public List<ClientReportDTO> getTopClients(LocalDate from, LocalDate to, int limit) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : null;

        List<ClientReportDTO> clientes = pedidoRepository.findTopClients(start, end);
        if (limit > 0 && clientes.size() > limit)
            return clientes.subList(0, limit);
        return clientes;
    }

    /**
     * Retorna os funcionários com mais pedidos atendidos no período.
     */
    public List<EmployeeReportDTO> getTopEmployees(LocalDate from, LocalDate to, int limit) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : null;

        List<EmployeeReportDTO> funcionarios = pedidoRepository.findTopEmployees(start, end);
        if (limit > 0 && funcionarios.size() > limit)
            return funcionarios.subList(0, limit);
        return funcionarios;
    }
}
