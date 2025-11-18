package dev.Service;

import dev.java.ManiFarma.DTO.ClientReportDTO;
import dev.java.ManiFarma.DTO.EmployeeReportDTO;
import dev.java.ManiFarma.DTO.ReportSummaryDTO;
import dev.java.ManiFarma.Entity.StatusPedido;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Repository.PedidoRepository;
import dev.java.ManiFarma.Service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private ReportService reportService;

    private Pedido pedido1;
    private Pedido pedido2;
    private ClientReportDTO clientReportDTO1;
    private ClientReportDTO clientReportDTO2;
    private EmployeeReportDTO employeeReportDTO1;
    private EmployeeReportDTO employeeReportDTO2;

    @BeforeEach
    void setUp() {
        // Pedidos
        pedido1 = new Pedido();
        pedido1.setId(1L);
        pedido1.setValorTotal(150.0);
        pedido1.setStatus(StatusPedido.PAGO);

        pedido2 = new Pedido();
        pedido2.setId(2L);
        pedido2.setValorTotal(200.0);
        pedido2.setStatus(StatusPedido.CONCLUIDO);

        // ClientReportDTOs
        clientReportDTO1 = new ClientReportDTO(1L, "João Silva", 5L, 500.0);
        clientReportDTO2 = new ClientReportDTO(2L, "Maria Santos", 3L, 300.0);

        // EmployeeReportDTOs
        employeeReportDTO1 = new EmployeeReportDTO(1L, "Carlos Farmacêutico", 10L, 1000.0);
        employeeReportDTO2 = new EmployeeReportDTO(2L, "Ana Atendente", 8L, 800.0);
    }



    @Test
    void deveGerarResumoComSucesso() {
        // Arrange
        when(pedidoRepository.countAllOrders()).thenReturn(100L);
        when(pedidoRepository.sumAllRevenue()).thenReturn(15000.0);
        when(pedidoRepository.countByStatus(StatusPedido.PENDENTE)).thenReturn(10L);
        when(pedidoRepository.countByStatus(StatusPedido.PAGO)).thenReturn(30L);
        when(pedidoRepository.countByStatus(StatusPedido.CONCLUIDO)).thenReturn(50L);
        when(pedidoRepository.countByStatus(StatusPedido.CANCELADO)).thenReturn(10L);

        // Act
        ReportSummaryDTO resultado = reportService.getSummary();

        // Assert
        assertNotNull(resultado);
        assertEquals(100L, resultado.getTotalPedidos());
        assertEquals(15000.0, resultado.getReceitaTotal());
        assertEquals(10L, resultado.getPendentes());
        assertEquals(30L, resultado.getPagos());
        assertEquals(50L, resultado.getConcluidos());
        assertEquals(10L, resultado.getCancelados());
        verify(pedidoRepository).countAllOrders();
        verify(pedidoRepository).sumAllRevenue();
        verify(pedidoRepository, times(4)).countByStatus(any(StatusPedido.class));
    }

    @Test
    void deveRetornarReceitaZeroQuandoNula() {
        // Arrange
        when(pedidoRepository.countAllOrders()).thenReturn(0L);
        when(pedidoRepository.sumAllRevenue()).thenReturn(null);
        when(pedidoRepository.countByStatus(any(StatusPedido.class))).thenReturn(0L);

        // Act
        ReportSummaryDTO resultado = reportService.getSummary();

        // Assert
        assertNotNull(resultado);
        assertEquals(0.0, resultado.getReceitaTotal());
    }

    @Test
    void deveGerarResumoComTodosStatusZerados() {
        // Arrange
        when(pedidoRepository.countAllOrders()).thenReturn(0L);
        when(pedidoRepository.sumAllRevenue()).thenReturn(0.0);
        when(pedidoRepository.countByStatus(any(StatusPedido.class))).thenReturn(0L);

        // Act
        ReportSummaryDTO resultado = reportService.getSummary();

        // Assert
        assertNotNull(resultado);
        assertEquals(0L, resultado.getTotalPedidos());
        assertEquals(0.0, resultado.getReceitaTotal());
        assertEquals(0L, resultado.getPendentes());
        assertEquals(0L, resultado.getPagos());
        assertEquals(0L, resultado.getConcluidos());
        assertEquals(0L, resultado.getCancelados());
    }



    @Test
    void deveRetornarPedidosEntreDatas() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findOrdersBetween(start, end))
                .thenReturn(Arrays.asList(pedido1, pedido2));

        // Act
        List<Pedido> resultado = reportService.getOrdersBetween(from, to);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoRepository).findOrdersBetween(start, end);
    }

    @Test
    void deveRetornarPedidosComDataInicialNula() {
        // Arrange
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findOrdersBetween(null, end))
                .thenReturn(Arrays.asList(pedido1));

        // Act
        List<Pedido> resultado = reportService.getOrdersBetween(null, to);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository).findOrdersBetween(null, end);
    }

    @Test
    void deveRetornarPedidosComDataFinalNula() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDateTime start = from.atStartOfDay();

        when(pedidoRepository.findOrdersBetween(start, null))
                .thenReturn(Arrays.asList(pedido1, pedido2));

        // Act
        List<Pedido> resultado = reportService.getOrdersBetween(from, null);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoRepository).findOrdersBetween(start, null);
    }

    @Test
    void deveRetornarPedidosComAmbasDataNulas() {
        // Arrange
        when(pedidoRepository.findOrdersBetween(null, null))
                .thenReturn(Arrays.asList(pedido1, pedido2));

        // Act
        List<Pedido> resultado = reportService.getOrdersBetween(null, null);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoRepository).findOrdersBetween(null, null);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaPedidosNoPeriodo() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findOrdersBetween(start, end))
                .thenReturn(Collections.emptyList());

        // Act
        List<Pedido> resultado = reportService.getOrdersBetween(from, to);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }


    @Test
    void deveRetornarTodosClientesQuandoLimiteZero() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findTopClients(start, end))
                .thenReturn(Arrays.asList(clientReportDTO1, clientReportDTO2));

        // Act
        List<ClientReportDTO> resultado = reportService.getTopClients(from, to, 0);

        // Assert
        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTodosClientesQuandoLimiteMaiorQueTotal() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findTopClients(start, end))
                .thenReturn(Arrays.asList(clientReportDTO1, clientReportDTO2));

        // Act
        List<ClientReportDTO> resultado = reportService.getTopClients(from, to, 10);

        // Assert
        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTopClientesComDatasNulas() {
        // Arrange
        when(pedidoRepository.findTopClients(null, null))
                .thenReturn(Arrays.asList(clientReportDTO1));

        // Act
        List<ClientReportDTO> resultado = reportService.getTopClients(null, null, 0);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository).findTopClients(null, null);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findTopClients(start, end))
                .thenReturn(Collections.emptyList());

        // Act
        List<ClientReportDTO> resultado = reportService.getTopClients(from, to, 5);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }


    @Test
    void deveRetornarTodosFuncionariosQuandoLimiteZero() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findTopEmployees(start, end))
                .thenReturn(Arrays.asList(employeeReportDTO1, employeeReportDTO2));

        // Act
        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(from, to, 0);

        // Assert
        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTodosFuncionariosQuandoLimiteMaiorQueTotal() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findTopEmployees(start, end))
                .thenReturn(Arrays.asList(employeeReportDTO1, employeeReportDTO2));

        // Act
        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(from, to, 10);

        // Assert
        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTopFuncionariosComDatasNulas() {
        // Arrange
        when(pedidoRepository.findTopEmployees(null, null))
                .thenReturn(Arrays.asList(employeeReportDTO1));

        // Act
        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(null, null, 0);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository).findTopEmployees(null, null);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaFuncionarios() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findTopEmployees(start, end))
                .thenReturn(Collections.emptyList());

        // Act
        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(from, to, 5);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveRetornarTopFuncionariosComLimiteNegativo() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        when(pedidoRepository.findTopEmployees(start, end))
                .thenReturn(Arrays.asList(employeeReportDTO1, employeeReportDTO2));

        // Act
        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(from, to, -1);

        // Assert
        assertEquals(2, resultado.size());
    }
}