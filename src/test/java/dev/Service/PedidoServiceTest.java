package dev.Service;

import dev.java.ManiFarma.DTO.*;
import dev.java.ManiFarma.Entity.*;
import dev.java.ManiFarma.Repository.*;
import dev.java.ManiFarma.Service.EmailService;
import dev.java.ManiFarma.Service.PaymentService;
import dev.java.ManiFarma.Service.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PedidoService pedidoService;

    private Cliente cliente;
    private Employee employee;
    private Produto produto;
    private Pedido pedido;
    private PedidoRequestDTO pedidoRequestDTO;

    @BeforeEach
    void setUp() {
        // Cliente
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");
        cliente.setTelefone("11999999999");

        // Employee
        employee = new Employee();
        employee.setId(2L);
        employee.setNome("Maria Farmacêutica");
        employee.setEmail("maria@farma.com");

        // Produto
        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Paracetamol");
        produto.setPreco(15.50);

        // Pedido
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setDescricao("Medicamentos diversos");
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setCliente(cliente);
        pedido.setEmployee(employee);
        pedido.setValorTotal(100.0);
        pedido.setReceita("receita.jpg");

        // PedidoRequestDTO
        pedidoRequestDTO = new PedidoRequestDTO();
        pedidoRequestDTO.setDescricao("Medicamentos");
        pedidoRequestDTO.setClienteId(1L);
        pedidoRequestDTO.setEmployeeId(2L);
        pedidoRequestDTO.setReceita("receita.jpg");
    }


    @Test
    void deveCriarPedidoComSucesso() {
        // Arrange
        PedidoProdutoRequestDTO itemDTO = new PedidoProdutoRequestDTO();
        itemDTO.setProdutoId(1L);
        itemDTO.setQuantidade(2);
        pedidoRequestDTO.setItens(Arrays.asList(itemDTO));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(paymentService.criarLinkDePagamento(any(Pedido.class))).thenReturn("http://pagamento.com/123");
        doNothing().when(emailService).enviarEmailPagamento(any(), any(), anyString());

        // Act
        PedidoResponseDTO resultado = pedidoService.criarPedido(pedidoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Medicamentos diversos", resultado.getDescricao());
        assertEquals(StatusPedido.PENDENTE, resultado.getStatus());
        verify(pedidoRepository).save(any(Pedido.class));
        verify(paymentService).criarLinkDePagamento(any(Pedido.class));
        verify(emailService).enviarEmailPagamento(any(), any(), anyString());
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        // Arrange
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        pedidoRequestDTO.setClienteId(99L);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            pedidoService.criarPedido(pedidoRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Cliente não encontrado"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveLancarExcecaoQuandoEmployeeNaoExiste() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        pedidoRequestDTO.setEmployeeId(99L);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            pedidoService.criarPedido(pedidoRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Funcionário não encontrado"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveCalcularValorTotalCorretamente() {
        // Arrange
        PedidoProdutoRequestDTO item1 = new PedidoProdutoRequestDTO();
        item1.setProdutoId(1L);
        item1.setQuantidade(2);

        Produto produto2 = new Produto();
        produto2.setId(2L);
        produto2.setNome("Ibuprofeno");
        produto2.setPreco(20.0);

        PedidoProdutoRequestDTO item2 = new PedidoProdutoRequestDTO();
        item2.setProdutoId(2L);
        item2.setQuantidade(3);

        pedidoRequestDTO.setItens(Arrays.asList(item1, item2));

        Pedido pedidoComValor = new Pedido();
        pedidoComValor.setId(1L);
        pedidoComValor.setValorTotal(91.0);
        pedidoComValor.setCliente(cliente);
        pedidoComValor.setEmployee(employee);
        pedidoComValor.setStatus(StatusPedido.PENDENTE);
        pedidoComValor.setDescricao("Medicamentos");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(produto2));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoComValor);
        when(paymentService.criarLinkDePagamento(any())).thenReturn("http://link.com");

        // Act
        PedidoResponseDTO resultado = pedidoService.criarPedido(pedidoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(91.0, resultado.getValorTotal());
    }

    @Test
    void deveCriarPedidoSemEmployeeQuandoNaoInformado() {
        // Arrange
        pedidoRequestDTO.setEmployeeId(null);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(paymentService.criarLinkDePagamento(any())).thenReturn("http://link.com");

        // Act
        PedidoResponseDTO resultado = pedidoService.criarPedido(pedidoRequestDTO);

        // Assert
        assertNotNull(resultado);
        verify(employeeRepository, never()).findById(any());
    }


    @Test
    void deveListarTodosOsPedidos() {
        // Arrange
        Pedido pedido2 = new Pedido();
        pedido2.setId(2L);
        pedido2.setCliente(cliente);
        pedido2.setStatus(StatusPedido.PAGO);

        when(pedidoRepository.findAll()).thenReturn(Arrays.asList(pedido, pedido2));

        // Act
        List<PedidoResponseDTO> resultado = pedidoService.getAllPedidos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoRepository).findAll();
    }

    @Test
    void deveBuscarPedidoPorId() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act
        PedidoResponseDTO resultado = pedidoService.getPedidoById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("João Silva", resultado.getClienteNome());
        verify(pedidoRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarPedidoInexistente() {
        // Arrange
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            pedidoService.getPedidoById(99L);
        });

        assertTrue(exception.getMessage().contains("Pedido não encontrado"));
    }

    @Test
    void deveListarPedidosPorCliente() {
        // Arrange
        when(pedidoRepository.findByClienteId(1L)).thenReturn(Arrays.asList(pedido));

        // Act
        List<PedidoResponseDTO> resultado = pedidoService.getPedidosPorCliente(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João Silva", resultado.get(0).getClienteNome());
        verify(pedidoRepository).findByClienteId(1L);
    }

    @Test
    void deveListarPedidosPorFuncionario() {
        // Arrange
        when(employeeRepository.existsById(2L)).thenReturn(true);
        when(pedidoRepository.findByEmployeeId(2L)).thenReturn(Arrays.asList(pedido));

        // Act
        List<PedidoResponseDTO> resultado = pedidoService.getPedidosPorFuncionario(2L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository).findByEmployeeId(2L);
    }

    @Test
    void deveLancarExcecaoAoListarPedidosDeFuncionarioInexistente() {
        // Arrange
        when(employeeRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            pedidoService.getPedidosPorFuncionario(99L);
        });

        assertTrue(exception.getMessage().contains("Funcionário não encontrado"));
    }

    @Test
    void deveListarPedidosPorStatus() {
        // Arrange
        when(pedidoRepository.findAll()).thenReturn(Arrays.asList(pedido));

        // Act
        List<PedidoResponseDTO> resultado = pedidoService.getPedidosPorStatus("PENDENTE");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(StatusPedido.PENDENTE, resultado.get(0).getStatus());
    }

    @Test
    void deveLancarExcecaoAoListarPedidosComStatusInvalido() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.getPedidosPorStatus("STATUS_INVALIDO");
        });

        assertTrue(exception.getMessage().contains("Status inválido"));
    }



    @Test
    void deveGerarLinkDePagamentoEEnviarEmail() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(paymentService.criarLinkDePagamento(any())).thenReturn("http://pagamento.com/123");
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        doNothing().when(emailService).enviarEmailPagamento(any(), any(), anyString());

        // Act
        pedidoService.gerarLinkEEnviarEmail(1L);

        // Assert
        verify(paymentService).criarLinkDePagamento(any(Pedido.class));
        verify(emailService).enviarEmailPagamento(any(), any(), anyString());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void deveLancarExcecaoAoGerarLinkComValorZero() {
        // Arrange
        pedido.setValorTotal(0.0);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.gerarLinkEEnviarEmail(1L);
        });

        assertTrue(exception.getMessage().contains("valor total R$0,00"));
        verify(paymentService, never()).criarLinkDePagamento(any());
        verify(emailService, never()).enviarEmailPagamento(any(), any(), anyString());
    }

    @Test
    void deveLancarExcecaoAoGerarLinkComValorNulo() {
        // Arrange
        pedido.setValorTotal(null);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.gerarLinkEEnviarEmail(1L);
        });

        assertTrue(exception.getMessage().contains("nulo"));
        verify(paymentService, never()).criarLinkDePagamento(any());
    }

    @Test
    void deveAlterarStatusParaEnvioDeCotacaoAoGerarLink() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(paymentService.criarLinkDePagamento(any())).thenReturn("http://link.com");
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            assertEquals(StatusPedido.ENVIODECOTACAO, p.getStatus());
            return p;
        });

        // Act
        pedidoService.gerarLinkEEnviarEmail(1L);

        // Assert
        verify(pedidoRepository).save(any(Pedido.class));
    }


    @Test
    void deveAtribuirFuncionarioAoPedido() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        pedidoService.atribuirFuncionario(1L, 2L);

        // Assert
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void deveLancarExcecaoAoAtribuirFuncionarioEmPedidoInexistente() {
        // Arrange
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            pedidoService.atribuirFuncionario(99L, 2L);
        });

        assertTrue(exception.getMessage().contains("Pedido não encontrado"));
    }

    @Test
    void deveLancarExcecaoAoAtribuirFuncionarioInexistente() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            pedidoService.atribuirFuncionario(1L, 99L);
        });

        assertTrue(exception.getMessage().contains("Funcionário não encontrado"));
    }


    @Test
    void deveAlterarStatusDoPedido() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        pedidoService.alterarStatus(1L, "PAGO",null );

        // Assert
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void deveAceitarStatusEmMinuscula() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        assertDoesNotThrow(() -> pedidoService.alterarStatus(1L, "pago",null));

        // Assert
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void deveLancarExcecaoAoAlterarParaStatusInvalido() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.alterarStatus(1L, "STATUS_INVALIDO", null);
        });

        assertTrue(exception.getMessage().contains("Status inválido"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveLancarExcecaoAoAlterarStatusDePedidoInexistente() {
        // Arrange
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            pedidoService.alterarStatus(99L, "PAGO", null);
        });

        assertTrue(exception.getMessage().contains("Pedido não encontrado"));
    }


    @Test
    void deveAdicionarItensAoPedido() {
        // Arrange
        pedido.setItens(new ArrayList<>());

        PedidoProdutoRequestDTO itemDTO = new PedidoProdutoRequestDTO();
        itemDTO.setProdutoId(1L);
        itemDTO.setQuantidade(2);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        PedidoResponseDTO resultado = pedidoService.adicionarItensAoPedido(1L, Arrays.asList(itemDTO));

        // Assert
        assertNotNull(resultado);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void deveRecalcularValorTotalAoAdicionarItens() {
        // Arrange
        pedido.setValorTotal(50.0);
        pedido.setItens(new ArrayList<>());

        PedidoProdutoRequestDTO itemDTO = new PedidoProdutoRequestDTO();
        itemDTO.setProdutoId(1L);
        itemDTO.setQuantidade(2);

        Pedido pedidoAtualizado = new Pedido();
        pedidoAtualizado.setId(1L);
        pedidoAtualizado.setValorTotal(81.0); // 50.0 + (15.50 * 2)
        pedidoAtualizado.setCliente(cliente);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoAtualizado);

        // Act
        PedidoResponseDTO resultado = pedidoService.adicionarItensAoPedido(1L, Arrays.asList(itemDTO));

        // Assert
        assertEquals(81.0, resultado.getValorTotal());
    }

    @Test
    void deveLancarExcecaoAoAdicionarItemComProdutoInexistente() {
        // Arrange
        PedidoProdutoRequestDTO itemDTO = new PedidoProdutoRequestDTO();
        itemDTO.setProdutoId(99L);
        itemDTO.setQuantidade(2);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            pedidoService.adicionarItensAoPedido(1L, Arrays.asList(itemDTO));
        });

        assertTrue(exception.getMessage().contains("Produto não encontrado"));
    }

    @Test
    void deveInicializarListaDeItensQuandoNula() {
        // Arrange
        pedido.setItens(null);
        pedido.setValorTotal(0.0);

        PedidoProdutoRequestDTO itemDTO = new PedidoProdutoRequestDTO();
        itemDTO.setProdutoId(1L);
        itemDTO.setQuantidade(1);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        assertDoesNotThrow(() -> pedidoService.adicionarItensAoPedido(1L, Arrays.asList(itemDTO)));

        // Assert
        verify(pedidoRepository).save(any(Pedido.class));
    }
}