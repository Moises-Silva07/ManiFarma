package dev.Controller;

import dev.java.ManiFarma.Controller.PedidoController;
import dev.java.ManiFarma.DTO.PedidoProdutoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoResponseDTO;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Entity.StatusPedido;
import dev.java.ManiFarma.Repository.PedidoRepository;
import dev.java.ManiFarma.Service.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoController pedidoController;

    private PedidoResponseDTO pedidoResponseDTO;
    private Pedido pedido;
    private MultipartFile receitaFile;

    @BeforeEach
    void setUp() {
        // PedidoResponseDTO
        pedidoResponseDTO = new PedidoResponseDTO();
        pedidoResponseDTO.setId(1L);
        pedidoResponseDTO.setDescricao("Medicamentos diversos");
        pedidoResponseDTO.setStatus(StatusPedido.PENDENTE);
        pedidoResponseDTO.setValorTotal(150.0);
        pedidoResponseDTO.setClienteNome("João Silva");

        // Pedido
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setDescricao("Medicamentos diversos");
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setValorTotal(150.0);
        pedido.setCaminhoReceita("uploads/receitas/receita_123.jpg");

        // MultipartFile mock
        receitaFile = new MockMultipartFile(
                "receita",
                "receita.jpg",
                "image/jpeg",
                "conteudo-da-imagem".getBytes()
        );
    }


    @Test
    void deveCriarPedidoComSucesso() {
        // Arrange
        when(pedidoService.criarPedidoMultipart(anyString(), any(StatusPedido.class), anyLong(), any(), any(MultipartFile.class)))
                .thenReturn(pedidoResponseDTO);

        // Act
        ResponseEntity<?> response = pedidoController.criarPedido("Medicamentos", 1L, receitaFile);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("Pedido criado com sucesso!", body.get("message"));
        assertNotNull(body.get("pedido"));

        verify(pedidoService).criarPedidoMultipart(anyString(), any(StatusPedido.class), anyLong(), any(), any(MultipartFile.class));
    }

    @Test
    void deveRetornarBadRequestQuandoArquivoInvalido() {
        // Arrange
        when(pedidoService.criarPedidoMultipart(anyString(), any(StatusPedido.class), anyLong(), any(), any(MultipartFile.class)))
                .thenThrow(new IllegalArgumentException("Tipo de arquivo inválido"));

        // Act
        ResponseEntity<?> response = pedidoController.criarPedido("Medicamentos", 1L, receitaFile);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("Tipo de arquivo inválido", body.get("error"));
    }

    @Test
    void deveRetornarNotFoundQuandoClienteNaoExiste() {
        // Arrange
        when(pedidoService.criarPedidoMultipart(anyString(), any(StatusPedido.class), anyLong(), any(), any(MultipartFile.class)))
                .thenThrow(new EntityNotFoundException("Cliente não encontrado"));

        // Act
        ResponseEntity<?> response = pedidoController.criarPedido("Medicamentos", 99L, receitaFile);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("Cliente não encontrado", body.get("error"));
    }

    @Test
    void deveRetornarInternalServerErrorQuandoOcorreErroInesperado() {
        // Arrange
        when(pedidoService.criarPedidoMultipart(anyString(), any(StatusPedido.class), anyLong(), any(), any(MultipartFile.class)))
                .thenThrow(new RuntimeException("Erro inesperado"));

        // Act
        ResponseEntity<?> response = pedidoController.criarPedido("Medicamentos", 1L, receitaFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertTrue(((String) body.get("error")).contains("Erro ao criar pedido"));
    }


    @Test
    void deveRetornarNotFoundQuandoPedidoNaoTemReceita() {
        // Arrange
        Pedido pedidoSemReceita = new Pedido();
        pedidoSemReceita.setId(1L);
        pedidoSemReceita.setCaminhoReceita(null);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSemReceita));

        // Act
        ResponseEntity<?> response = pedidoController.visualizarReceita(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Este pedido não possui imagem de receita", body.get("error"));
    }

    @Test
    void deveRetornarNotFoundQuandoPedidoNaoExiste() {
        // Arrange
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = pedidoController.visualizarReceita(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Pedido não encontrado", body.get("error"));
    }



    @Test
    void deveListarTodosPedidosComSucesso() {
        // Arrange
        List<PedidoResponseDTO> pedidos = Arrays.asList(pedidoResponseDTO);
        when(pedidoService.getAllPedidos()).thenReturn(pedidos);

        // Act
        ResponseEntity<?> response = pedidoController.listarTodosPedidos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        List<PedidoResponseDTO> body = (List<PedidoResponseDTO>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        verify(pedidoService).getAllPedidos();
    }

    @Test
    void deveRetornarErroAoListarTodosPedidos() {
        // Arrange
        when(pedidoService.getAllPedidos()).thenThrow(new RuntimeException("Erro no banco"));

        // Act
        ResponseEntity<?> response = pedidoController.listarTodosPedidos();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("error").contains("Erro ao listar todos os pedidos"));
    }

    @Test
    void deveBuscarPedidoPorIdComSucesso() {
        // Arrange
        when(pedidoService.getPedidoById(1L)).thenReturn(pedidoResponseDTO);

        // Act
        ResponseEntity<?> response = pedidoController.buscarPedidoPorId(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        PedidoResponseDTO body = (PedidoResponseDTO) response.getBody();
        assertNotNull(body);
        assertEquals(1L, body.getId());
        verify(pedidoService).getPedidoById(1L);
    }

    @Test
    void deveRetornarNotFoundQuandoPedidoNaoExistePorId() {
        // Arrange
        when(pedidoService.getPedidoById(99L)).thenThrow(new EntityNotFoundException("Pedido não encontrado"));

        // Act
        ResponseEntity<?> response = pedidoController.buscarPedidoPorId(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Pedido não encontrado", body.get("error"));
    }

    @Test
    void deveBuscarPedidosPorClienteComSucesso() {
        // Arrange
        List<PedidoResponseDTO> pedidos = Arrays.asList(pedidoResponseDTO);
        when(pedidoService.getPedidosPorCliente(1L)).thenReturn(pedidos);

        // Act
        ResponseEntity<?> response = pedidoController.buscarPedidosPorCliente(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        List<PedidoResponseDTO> body = (List<PedidoResponseDTO>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        verify(pedidoService).getPedidosPorCliente(1L);
    }

    @Test
    void deveRetornarErroAoBuscarPedidosPorCliente() {
        // Arrange
        when(pedidoService.getPedidosPorCliente(1L)).thenThrow(new RuntimeException("Erro no banco"));

        // Act
        ResponseEntity<?> response = pedidoController.buscarPedidosPorCliente(1L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deveBuscarPedidosPorFuncionarioComSucesso() {
        // Arrange
        List<PedidoResponseDTO> pedidos = Arrays.asList(pedidoResponseDTO);
        when(pedidoService.getPedidosPorFuncionario(2L)).thenReturn(pedidos);

        // Act
        ResponseEntity<?> response = pedidoController.buscarPedidosPorFuncionario(2L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        List<PedidoResponseDTO> body = (List<PedidoResponseDTO>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        verify(pedidoService).getPedidosPorFuncionario(2L);
    }

    @Test
    void deveRetornarNotFoundQuandoFuncionarioNaoExiste() {
        // Arrange
        when(pedidoService.getPedidosPorFuncionario(99L))
                .thenThrow(new EntityNotFoundException("Funcionário não encontrado"));

        // Act
        ResponseEntity<?> response = pedidoController.buscarPedidosPorFuncionario(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Funcionário não encontrado", body.get("error"));
    }

    @Test
    void deveListarPedidosPorStatusComSucesso() {
        // Arrange
        List<PedidoResponseDTO> pedidos = Arrays.asList(pedidoResponseDTO);
        when(pedidoService.getPedidosPorStatus("PENDENTE")).thenReturn(pedidos);

        // Act
        ResponseEntity<?> response = pedidoController.listarPedidosPorStatus("PENDENTE");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        List<PedidoResponseDTO> body = (List<PedidoResponseDTO>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        verify(pedidoService).getPedidosPorStatus("PENDENTE");
    }

    @Test
    void deveRetornarBadRequestQuandoStatusInvalido() {
        // Arrange
        when(pedidoService.getPedidosPorStatus("INVALIDO"))
                .thenThrow(new IllegalArgumentException("Status inválido"));

        // Act
        ResponseEntity<?> response = pedidoController.listarPedidosPorStatus("INVALIDO");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Status inválido", body.get("error"));
    }



    @Test
    void deveEnviarCotacaoComSucesso() {
        // Arrange
        doNothing().when(pedidoService).gerarLinkEEnviarEmail(1L);

        // Act
        ResponseEntity<?> response = pedidoController.enviarCotacao(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("Link de cotação gerado e e-mail enviado com sucesso!", body.get("message"));
        verify(pedidoService).gerarLinkEEnviarEmail(1L);
    }

    @Test
    void deveRetornarNotFoundAoEnviarCotacaoDePedidoInexistente() {
        // Arrange
        doThrow(new EntityNotFoundException("Pedido não encontrado"))
                .when(pedidoService).gerarLinkEEnviarEmail(99L);

        // Act
        ResponseEntity<?> response = pedidoController.enviarCotacao(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("Pedido não encontrado", body.get("error"));
    }

    @Test
    void deveRetornarBadRequestQuandoPedidoSemValor() {
        // Arrange
        doThrow(new RuntimeException("Pedido sem valor total"))
                .when(pedidoService).gerarLinkEEnviarEmail(1L);

        // Act
        ResponseEntity<?> response = pedidoController.enviarCotacao(1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }


    @Test
    void deveAtribuirFuncionarioComSucesso() {
        // Arrange
        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("employeeId", 2L);

        doNothing().when(pedidoService).atribuirFuncionario(1L, 2L);

        // Act
        ResponseEntity<?> response = pedidoController.atribuirFuncionario(1L, requestBody);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("Funcionário atribuído com sucesso!", body.get("message"));
        verify(pedidoService).atribuirFuncionario(1L, 2L);
    }

    @Test
    void deveRetornarBadRequestQuandoEmployeeIdNulo() {
        // Arrange
        Map<String, Long> requestBody = new HashMap<>();

        // Act
        ResponseEntity<?> response = pedidoController.atribuirFuncionario(1L, requestBody);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("O campo 'employeeId' é obrigatório", body.get("error"));
        verify(pedidoService, never()).atribuirFuncionario(anyLong(), anyLong());
    }

    @Test
    void deveRetornarNotFoundAoAtribuirFuncionarioInexistente() {
        // Arrange
        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("employeeId", 99L);

        doThrow(new EntityNotFoundException("Funcionário não encontrado"))
                .when(pedidoService).atribuirFuncionario(1L, 99L);

        // Act
        ResponseEntity<?> response = pedidoController.atribuirFuncionario(1L, requestBody);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("Funcionário não encontrado", body.get("error"));
    }



    @Test
    void deveAlterarStatusComSucesso() {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "PAGO");

        doNothing().when(pedidoService).alterarStatus(1L, "PAGO");

        // Act
        ResponseEntity<?> response = pedidoController.alterarStatus(1L, requestBody);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("Status atualizado com sucesso!", body.get("message"));
        assertEquals("PAGO", body.get("novoStatus"));
        verify(pedidoService).alterarStatus(1L, "PAGO");
    }

    @Test
    void deveRetornarBadRequestQuandoStatusNuloOuVazio() {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "");

        // Act
        ResponseEntity<?> response = pedidoController.alterarStatus(1L, requestBody);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("O campo 'status' é obrigatório", body.get("error"));
        verify(pedidoService, never()).alterarStatus(anyLong(), anyString());
    }

    @Test
    void deveRetornarBadRequestQuandoStatusInvalidoAoAlterar() {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "INVALIDO");

        doThrow(new IllegalArgumentException("Status inválido"))
                .when(pedidoService).alterarStatus(1L, "INVALIDO");

        // Act
        ResponseEntity<?> response = pedidoController.alterarStatus(1L, requestBody);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("Status inválido", body.get("error"));
    }

    @Test
    void deveRetornarNotFoundAoAlterarStatusDePedidoInexistente() {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "PAGO");

        doThrow(new EntityNotFoundException("Pedido não encontrado"))
                .when(pedidoService).alterarStatus(99L, "PAGO");

        // Act
        ResponseEntity<?> response = pedidoController.alterarStatus(99L, requestBody);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void deveAdicionarItensAoPedidoComSucesso() {
        // Arrange
        PedidoProdutoRequestDTO item = new PedidoProdutoRequestDTO();
        item.setProdutoId(1L);
        item.setQuantidade(2);

        List<PedidoProdutoRequestDTO> itens = Arrays.asList(item);

        when(pedidoService.adicionarItensAoPedido(1L, itens)).thenReturn(pedidoResponseDTO);

        // Act
        ResponseEntity<?> response = pedidoController.adicionarItensAoPedido(1L, itens);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        PedidoResponseDTO body = (PedidoResponseDTO) response.getBody();
        assertNotNull(body);
        assertEquals(1L, body.getId());
        verify(pedidoService).adicionarItensAoPedido(1L, itens);
    }

    @Test
    void deveRetornarNotFoundAoAdicionarItensEmPedidoInexistente() {
        // Arrange
        PedidoProdutoRequestDTO item = new PedidoProdutoRequestDTO();
        item.setProdutoId(1L);
        item.setQuantidade(2);

        List<PedidoProdutoRequestDTO> itens = Arrays.asList(item);

        when(pedidoService.adicionarItensAoPedido(99L, itens))
                .thenThrow(new EntityNotFoundException("Pedido não encontrado"));

        // Act
        ResponseEntity<?> response = pedidoController.adicionarItensAoPedido(99L, itens);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Pedido não encontrado", body.get("error"));
    }

    @Test
    void deveRetornarNotFoundAoAdicionarItemComProdutoInexistente() {
        // Arrange
        PedidoProdutoRequestDTO item = new PedidoProdutoRequestDTO();
        item.setProdutoId(99L);
        item.setQuantidade(2);

        List<PedidoProdutoRequestDTO> itens = Arrays.asList(item);

        when(pedidoService.adicionarItensAoPedido(1L, itens))
                .thenThrow(new EntityNotFoundException("Produto não encontrado"));

        // Act
        ResponseEntity<?> response = pedidoController.adicionarItensAoPedido(1L, itens);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Produto não encontrado", body.get("error"));
    }

    @Test
    void deveRetornarInternalServerErrorAoAdicionarItens() {
        // Arrange
        PedidoProdutoRequestDTO item = new PedidoProdutoRequestDTO();
        item.setProdutoId(1L);
        item.setQuantidade(2);

        List<PedidoProdutoRequestDTO> itens = Arrays.asList(item);

        when(pedidoService.adicionarItensAoPedido(1L, itens))
                .thenThrow(new RuntimeException("Erro inesperado"));

        // Act
        ResponseEntity<?> response = pedidoController.adicionarItensAoPedido(1L, itens);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("error").contains("Erro ao adicionar itens ao pedido"));
    }
}