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

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        pedido1 = new Pedido();
        pedido1.setId(1L);
        pedido1.setValorTotal(150.0);
        pedido1.setStatus(StatusPedido.PAGO);

        pedido2 = new Pedido();
        pedido2.setId(2L);
        pedido2.setValorTotal(200.0);
        pedido2.setStatus(StatusPedido.CONCLUIDO);

        start = LocalDateTime.of(2024, 1, 1, 0, 0);
        end = LocalDateTime.of(2024, 12, 31, 23, 59);
    }

    // ========== TESTES DE getSummary() ==========

    @Test
    void deveGerarResumoComSucesso() {
        when(pedidoRepository.countAllOrders(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(100L);
        when(pedidoRepository.sumAllRevenue(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(15000.0);
        when(pedidoRepository.countByStatus(eq(StatusPedido.PENDENTE), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(10L);
        when(pedidoRepository.countByStatus(eq(StatusPedido.PAGO), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(30L);
        when(pedidoRepository.countByStatus(eq(StatusPedido.CONCLUIDO), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(50L);
        when(pedidoRepository.countByStatus(eq(StatusPedido.CANCELADO), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(10L);

        ReportSummaryDTO resultado = reportService.getSummary(start, end);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getTotalPedidos());
        assertEquals(15000.0, resultado.getReceitaTotal());
        assertEquals(10L, resultado.getPendentes());
        assertEquals(30L, resultado.getPagos());
        assertEquals(50L, resultado.getConcluidos());
        assertEquals(10L, resultado.getCancelados());
        verify(pedidoRepository).countAllOrders(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(pedidoRepository).sumAllRevenue(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(pedidoRepository, times(4)).countByStatus(any(StatusPedido.class), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void deveRetornarReceitaZeroQuandoNula() {
        when(pedidoRepository.countAllOrders(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);
        when(pedidoRepository.sumAllRevenue(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(null);
        when(pedidoRepository.countByStatus(any(StatusPedido.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);

        ReportSummaryDTO resultado = reportService.getSummary(start, end);

        assertNotNull(resultado);
        assertEquals(0.0, resultado.getReceitaTotal());
    }

    @Test
    void deveGerarResumoComTodosStatusZerados() {
        when(pedidoRepository.countAllOrders(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);
        when(pedidoRepository.sumAllRevenue(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0.0);
        when(pedidoRepository.countByStatus(any(StatusPedido.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);

        ReportSummaryDTO resultado = reportService.getSummary(start, end);

        assertNotNull(resultado);
        assertEquals(0L, resultado.getTotalPedidos());
        assertEquals(0.0, resultado.getReceitaTotal());
        assertEquals(0L, resultado.getPendentes());
        assertEquals(0L, resultado.getPagos());
        assertEquals(0L, resultado.getConcluidos());
        assertEquals(0L, resultado.getCancelados());
    }

    @Test
    void deveGerarResumoComDatasNulas() {
        when(pedidoRepository.countAllOrders(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(100L);
        when(pedidoRepository.sumAllRevenue(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(15000.0);
        when(pedidoRepository.countByStatus(any(StatusPedido.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(10L);

        ReportSummaryDTO resultado = reportService.getSummary(null, null);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getTotalPedidos());
        assertEquals(15000.0, resultado.getReceitaTotal());
        verify(pedidoRepository).countAllOrders(eq(LocalDateTime.of(2000, 1, 1, 0, 0)), any(LocalDateTime.class));
    }

    // ========== TESTES DE getOrdersBetween() ==========

    @Test
    void deveRetornarPedidosEntreDatas() {
        List<Pedido> pedidos = new ArrayList<>();
        pedidos.add(pedido1);
        pedidos.add(pedido2);

        when(pedidoRepository.findOrdersBetween(start, end)).thenReturn(pedidos);

        List<Pedido> resultado = reportService.getOrdersBetween(start, end);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoRepository).findOrdersBetween(start, end);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaPedidosNoPeriodo() {
        when(pedidoRepository.findOrdersBetween(start, end))
                .thenReturn(Collections.emptyList());

        List<Pedido> resultado = reportService.getOrdersBetween(start, end);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // ========== TESTES DE getTopClients() ==========

    @Test
    void deveRetornarTopClientesComSucesso() {
        List<ClientReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new ClientReportDTO(1L, "João Silva", "joao@email.com", 5L, 500.0));
        mockResults.add(new ClientReportDTO(2L, "Maria Santos", "maria@email.com", 3L, 300.0));

        when(pedidoRepository.findTopClients(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockResults);

        List<ClientReportDTO> resultado = reportService.getTopClients(start, end, 2);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertEquals("Maria Santos", resultado.get(1).getNome());
    }

    @Test
    void deveRetornarTopClientesComLimite() {
        List<ClientReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new ClientReportDTO(1L, "João Silva", "joao@email.com", 5L, 500.0));
        mockResults.add(new ClientReportDTO(2L, "Maria Santos", "maria@email.com", 3L, 300.0));
        mockResults.add(new ClientReportDTO(3L, "Pedro Oliveira", "pedro@email.com", 2L, 200.0));

        when(pedidoRepository.findTopClients(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockResults);

        List<ClientReportDTO> resultado = reportService.getTopClients(start, end, 2);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTodosClientesQuandoLimiteZero() {
        List<ClientReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new ClientReportDTO(1L, "João Silva", "joao@email.com", 5L, 500.0));
        mockResults.add(new ClientReportDTO(2L, "Maria Santos", "maria@email.com", 3L, 300.0));

        when(pedidoRepository.findTopClients(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockResults);

        List<ClientReportDTO> resultado = reportService.getTopClients(start, end, 0);

        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTodosClientesQuandoLimiteMaiorQueTotal() {
        List<ClientReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new ClientReportDTO(1L, "João Silva", "joao@email.com", 5L, 500.0));
        mockResults.add(new ClientReportDTO(2L, "Maria Santos", "maria@email.com", 3L, 300.0));

        when(pedidoRepository.findTopClients(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockResults);

        List<ClientReportDTO> resultado = reportService.getTopClients(start, end, 10);

        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTopClientesComDatasNulas() {
        List<ClientReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new ClientReportDTO(1L, "João Silva", "joao@email.com", 5L, 500.0));

        when(pedidoRepository.findTopClients(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResults);

        List<ClientReportDTO> resultado = reportService.getTopClients(null, null, 0);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository).findTopClients(eq(LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0)), any(LocalDateTime.class));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        when(pedidoRepository.findTopClients(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<ClientReportDTO> resultado = reportService.getTopClients(start, end, 5);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // ========== TESTES DE getTopEmployees() ==========

    @Test
    void deveRetornarTopFuncionariosComSucesso() {
        List<EmployeeReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new EmployeeReportDTO(1L, "Carlos Farmacêutico", 10L, 1000.0));
        mockResults.add(new EmployeeReportDTO(2L, "Ana Atendente", 8L, 800.0));

        when(pedidoRepository.findTopEmployees(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockResults);

        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(start, end, 2);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Carlos Farmacêutico", resultado.get(0).getNome());
        assertEquals("Ana Atendente", resultado.get(1).getNome());
    }

    @Test
    void deveRetornarTopFuncionariosComLimite() {
        List<EmployeeReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new EmployeeReportDTO(1L, "Carlos", 10L, 1000.0));
        mockResults.add(new EmployeeReportDTO(2L, "Ana", 8L, 800.0));
        mockResults.add(new EmployeeReportDTO(3L, "José", 6L, 600.0));

        when(pedidoRepository.findTopEmployees(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockResults);

        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(start, end, 2);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTodosFuncionariosQuandoLimiteZero() {
        List<EmployeeReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new EmployeeReportDTO(1L, "Carlos", 10L, 1000.0));
        mockResults.add(new EmployeeReportDTO(2L, "Ana", 8L, 800.0));

        when(pedidoRepository.findTopEmployees(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockResults);

        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(start, end, 0);

        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTodosFuncionariosQuandoLimiteMaiorQueTotal() {
        List<EmployeeReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new EmployeeReportDTO(1L, "Carlos", 10L, 1000.0));
        mockResults.add(new EmployeeReportDTO(2L, "Ana", 8L, 800.0));

        when(pedidoRepository.findTopEmployees(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockResults);

        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(start, end, 10);

        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarTopFuncionariosComDatasNulas() {
        List<EmployeeReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new EmployeeReportDTO(1L, "Carlos", 10L, 1000.0));

        when(pedidoRepository.findTopEmployees(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResults);

        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(null, null, 0);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository).findTopEmployees(eq(LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0)), any(LocalDateTime.class));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaFuncionarios() {
        when(pedidoRepository.findTopEmployees(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(start, end, 5);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveRetornarTopFuncionariosComLimiteNegativo() {
        List<EmployeeReportDTO> mockResults = new ArrayList<>();
        mockResults.add(new EmployeeReportDTO(1L, "Carlos", 10L, 1000.0));
        mockResults.add(new EmployeeReportDTO(2L, "Ana", 8L, 800.0));

        when(pedidoRepository.findTopEmployees(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockResults);

        List<EmployeeReportDTO> resultado = reportService.getTopEmployees(start, end, -1);

        assertEquals(2, resultado.size());
    }
}