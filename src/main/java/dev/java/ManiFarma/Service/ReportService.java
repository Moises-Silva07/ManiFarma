package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.ClientReportDTO;
import dev.java.ManiFarma.DTO.EmployeeReportDTO;
import dev.java.ManiFarma.DTO.ReportSummaryDTO;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Entity.StatusPedido;
import dev.java.ManiFarma.Repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final PedidoRepository pedidoRepository;

    public ReportService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public ReportSummaryDTO getSummary(LocalDateTime start, LocalDateTime end) {
        if (start == null) start = LocalDateTime.of(2000, 1, 1, 0, 0);
        if (end == null) end = LocalDateTime.now();

        long totalPedidos = pedidoRepository.countAllOrders(start, end);
        Double receitaTotal = pedidoRepository.sumAllRevenue(start, end);
        if (receitaTotal == null) receitaTotal = 0.0;

        long pendentes = pedidoRepository.countByStatus(StatusPedido.PENDENTE, start, end);
        long pagos = pedidoRepository.countByStatus(StatusPedido.PAGO, start, end);
        long concluidos = pedidoRepository.countByStatus(StatusPedido.CONCLUIDO, start, end);
        long cancelados = pedidoRepository.countByStatus(StatusPedido.CANCELADO, start, end);

        return new ReportSummaryDTO(totalPedidos, receitaTotal, pendentes, pagos, concluidos, cancelados);
    }

    public List<Pedido> getOrdersBetween(LocalDateTime from, LocalDateTime to) {
        return pedidoRepository.findOrdersBetween(from, to);
    }

    public List<ClientReportDTO> getTopClients(LocalDateTime start, LocalDateTime end, int limit) {
        if (start == null) start = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        if (end == null) end = LocalDateTime.now();


        List<ClientReportDTO> clientes = pedidoRepository.findTopClients(start, end);

        if (limit > 0 && clientes.size() > limit) {
            return clientes.subList(0, limit);
        }
        return clientes;
    }

    public List<EmployeeReportDTO> getTopEmployees(LocalDateTime start, LocalDateTime end, int limit) {
        if (start == null) start = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        if (end == null) end = LocalDateTime.now();

        List<EmployeeReportDTO> funcionarios = pedidoRepository.findTopEmployees(start, end);

        if (limit > 0 && funcionarios.size() > limit) {
            return funcionarios.subList(0, limit);
        }
        return funcionarios;
    }
}