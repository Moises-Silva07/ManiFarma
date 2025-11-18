package dev.Service;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PreferenceClient preferenceClient; // Não é usado diretamente, será mockado via construção

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setDescricao("Medicamentos diversos");
        pedido.setValorTotal(150.75);
    }



    @Test
    void deveCriarLinkDePagamentoComSucesso() throws Exception {

        Preference preferenceMock = mock(Preference.class);
        when(preferenceMock.getInitPoint()).thenReturn("http://pagamento.com/link123");

        try (MockedConstruction<PreferenceClient> mockClient =
                     mockConstruction(PreferenceClient.class,
                             (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preferenceMock)
                     )) {

            String link = paymentService.criarLinkDePagamento(pedido);

            assertNotNull(link);
            assertEquals("http://pagamento.com/link123", link);
        }
    }

    @Test
    void deveLancarExcecaoQuandoApiMercadoPagoFalhar() throws Exception {

        try (MockedConstruction<PreferenceClient> mockClient =
                     mockConstruction(PreferenceClient.class,
                             (mock, context) -> when(mock.create(any())).thenThrow(new RuntimeException("Erro MP"))
                     )) {

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                paymentService.criarLinkDePagamento(pedido);
            });

            assertTrue(exception.getMessage().contains("Falha ao gerar link"));
        }
    }

    @Test
    void deveCriarItemComValorCorreto() throws Exception {

        try (MockedConstruction<PreferenceClient> mockClient =
                     mockConstruction(PreferenceClient.class,
                             (mock, context) -> {
                                 when(mock.create(any(PreferenceRequest.class))).thenAnswer(invocation -> {
                                     PreferenceRequest req = invocation.getArgument(0);

                                     PreferenceItemRequest item = req.getItems().get(0);

                                     assertEquals(new BigDecimal("150.75"), item.getUnitPrice());
                                     assertEquals("Orçamento ManiFarma #1", item.getTitle());
                                     assertEquals("Medicamentos diversos", item.getDescription());
                                     assertEquals("BRL", item.getCurrencyId());
                                     assertEquals(1, item.getQuantity());
                                     assertEquals("1", item.getId());

                                     Preference pref = mock(Preference.class);
                                     when(pref.getInitPoint()).thenReturn("http://teste.com");
                                     return pref;
                                 });
                             })) {

            String link = paymentService.criarLinkDePagamento(pedido);

            assertEquals("http://teste.com", link);
        }
    }
}
