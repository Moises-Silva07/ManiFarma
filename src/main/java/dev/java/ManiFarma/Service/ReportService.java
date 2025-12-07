package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.ClientReportDTO;
import dev.java.ManiFarma.DTO.EmployeeReportDTO;
import dev.java.ManiFarma.DTO.ReportSummaryDTO;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Entity.StatusPedido;
import dev.java.ManiFarma.Repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final PedidoRepository pedidoRepository;

    public ReportService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

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

    public List<Pedido> getOrdersBetween(LocalDateTime from, LocalDateTime to) {
        return pedidoRepository.findOrdersBetween(from, to);
    }

    public List<ClientReportDTO> getTopClients(LocalDateTime start, LocalDateTime end, int limit) {
        if (start == null) {
            start = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0); // Data padrão
        }
        if (end == null) {
            end = LocalDateTime.now(); // Data atual
        }

        List<Object[]> results = pedidoRepository.findTopClients(start, end);

        List<ClientReportDTO> clientes = results.stream()
                .map(result -> new ClientReportDTO(
                        (Long) result[0],  // id
                        (String) result[1], // nome
                        (String) result[2], // email
                        (Long) result[3],   // número de pedidos
                        (Double) result[4]  // total gasto
                ))
                .collect(Collectors.toList());

        if (limit > 0 && clientes.size() > limit) {
            return clientes.subList(0, limit);
        }

        return clientes;
    }

    public List<EmployeeReportDTO> getTopEmployees(LocalDateTime start, LocalDateTime end, int limit) {
        if (start == null) {
            start = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0); // Data padrão
        }
        if (end == null) {
            end = LocalDateTime.now(); // Data atual
        }

        List<EmployeeReportDTO> funcionarios = pedidoRepository.findTopEmployees(start, end);

        if (limit > 0 && funcionarios.size() > limit) {
            return funcionarios.subList(0, limit);
        }

        return funcionarios;
    }
}
