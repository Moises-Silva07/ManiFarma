package dev.java.ManiFarma.Service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import dev.java.ManiFarma.Entity.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    @Value("${mercadopago.access_token}")
    private String mercadoPagoAccessToken;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
    }

    public String criarLinkDePagamento(Pedido pedido) {
        try {
            // Garante que o valor total não seja nulo, definindo 0.0 como padrão
            Double valor = (pedido.getValorTotal() != null) ? pedido.getValorTotal() : 0.0;
            
            // Converte o valor para BigDecimal, que é o tipo esperado pela API do MP
            BigDecimal valorTotalBD = new BigDecimal(valor);

            // Cria um único item de preferência com os dados do pedido
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(pedido.getId().toString()) // ID do pedido
                    .title("Orçamento Pedido #" + pedido.getId()) // Título dinâmico
                    .description(pedido.getDescricao()) // Descrição do pedido
                    .quantity(1) // Quantidade é 1 (representa o orçamento total)
                    .currencyId("BRL") // Moeda
                    .unitPrice(valorTotalBD) // Preço unitário é o valor total do pedido
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);
            
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items).build();
            
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            
            return preference.getInitPoint(); // Este é o link!

        } catch (Exception e) {
            System.err.println("Erro ao criar link de pagamento: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao gerar link de pagamento.");
        }
    }
}