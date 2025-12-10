package dev.Service;

import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Entity.StatusPedido;
import dev.java.ManiFarma.Service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private Cliente cliente;
    private Pedido pedido;
    private String linkPagamento;

    @BeforeEach
    void setUp() {
        // Configura o email de origem usando ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@manifarma.com");

        // Cliente
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");
        cliente.setTelefone("11999999999");

        // Pedido
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setDescricao("Medicamentos diversos");
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setValorTotal(150.50);
        pedido.setCliente(cliente);

        // Link de pagamento
        linkPagamento = "http://pagamento.manifarma.com/pedido/1";
    }


    @Test
    void deveEnviarEmailComSucesso() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagemEnviada = messageCaptor.getValue();

        assertNotNull(mensagemEnviada);
        assertEquals("noreply@manifarma.com", mensagemEnviada.getFrom());
        assertArrayEquals(new String[]{"joao@email.com"}, mensagemEnviada.getTo());
        assertEquals("ManiFarma: Cotação do Pedido #1", mensagemEnviada.getSubject());
        assertTrue(mensagemEnviada.getText().contains("João Silva"));
        assertTrue(mensagemEnviada.getText().contains("R$ 150,50"));
        assertTrue(mensagemEnviada.getText().contains(linkPagamento));
    }

    @Test
    void deveIncluirNomeDoClienteNoCorpoDoEmail() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertTrue(mensagem.getText().startsWith("Olá, João Silva!"));
    }

    @Test
    void deveIncluirIdDoPedidoNoAssuntoECorpo() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertTrue(mensagem.getSubject().contains("#1"));
        assertTrue(mensagem.getText().contains("pedido #1"));
    }

    @Test
    void deveFormatarValorEmReais() {
        // Arrange
        pedido.setValorTotal(1500.75);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertTrue(mensagem.getText().contains("R$ 1.500,75"));
    }

    @Test
    void deveIncluirLinkDePagamentoNoEmail() {
        // Arrange
        String linkCustomizado = "https://custom-payment.com/abc123";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkCustomizado);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertTrue(mensagem.getText().contains(linkCustomizado));
    }

    @Test
    void deveIncluirAssinaturaManiFarmaNoEmail() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertTrue(mensagem.getText().contains("Atenciosamente"));
        assertTrue(mensagem.getText().contains("Equipe ManiFarma"));
    }

    @Test
    void deveEnviarEmailParaEnderecoCorretoDoCliente() {
        // Arrange
        cliente.setEmail("maria.santos@example.com");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertArrayEquals(new String[]{"maria.santos@example.com"}, mensagem.getTo());
    }

    @Test
    void deveUtilizarEmailDeOrigemConfigurado() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "fromEmail", "contato@manifarma.com.br");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertEquals("contato@manifarma.com.br", mensagem.getFrom());
    }


    @Test
    void deveFormatarValorPequenoCorretamente() {
        // Arrange
        pedido.setValorTotal(9.99);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertTrue(mensagem.getText().contains("R$ 9,99"));
    }

    @Test
    void deveFormatarValorGrandeCorretamente() {
        // Arrange
        pedido.setValorTotal(123456.78);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertTrue(mensagem.getText().contains("R$ 123.456,78"));
    }

    @Test
    void deveFormatarValorInteiroCorretamente() {
        // Arrange
        pedido.setValorTotal(100.0);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertTrue(mensagem.getText().contains("R$ 100,00"));
    }


    @Test
    void naoDeveLancarExcecaoQuandoEnvioFalha() {
        // Arrange
        doThrow(new RuntimeException("Erro de conexão")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);
        });

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void deveTentarEnviarEmailMesmoComDadosMinimos() {
        // Arrange
        Cliente clienteMinimo = new Cliente();
        clienteMinimo.setEmail("teste@email.com");
        clienteMinimo.setNome("Cliente Teste");

        Pedido pedidoMinimo = new Pedido();
        pedidoMinimo.setId(999L);
        pedidoMinimo.setValorTotal(50.0);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(clienteMinimo, pedidoMinimo, "http://link.com");

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        assertNotNull(mensagem);
        assertNotNull(mensagem.getTo());
        assertNotNull(mensagem.getSubject());
        assertNotNull(mensagem.getText());
    }



    @Test
    void deveConterTodasSecoesPrincipaisDoEmail() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();
        String texto = mensagem.getText();

        // Verifica saudação
        assertTrue(texto.contains("Olá"));

        // Verifica informação da cotação
        assertTrue(texto.contains("cotação"));

        // Verifica valor
        assertTrue(texto.contains("Valor total"));

        // Verifica instrução de pagamento
        assertTrue(texto.contains("Para realizar o pagamento"));

        // Verifica despedida
        assertTrue(texto.contains("Atenciosamente"));
    }

    @Test
    void deveEnviarEmailApenasUmaVez() {
        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void deveFormatarEmailComQuebrasDeLinha() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailPagamento(cliente, pedido, linkPagamento);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();

        // Verifica que há quebras de linha no texto
        assertTrue(mensagem.getText().contains("\n"));

        // Verifica estrutura básica com quebras de linha
        String[] linhas = mensagem.getText().split("\n");
        assertTrue(linhas.length > 5, "Email deve ter múltiplas linhas");
    }
}