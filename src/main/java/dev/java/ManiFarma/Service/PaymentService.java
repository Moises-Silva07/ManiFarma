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
            Double valorTotal = pedido.getValorTotal();
            if (valorTotal == null || valorTotal <= 0) {
                throw new RuntimeException("Pedido ID " + pedido.getId() + " está sem valor total ou com valor zero. Não é possível gerar cotação.");
            }

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(pedido.getId().toString())
                    .title("Orçamento ManiFarma #" + pedido.getId())
                    .description(pedido.getDescricao())
                    .quantity(1)
                    .currencyId("BRL")
                    .unitPrice(new BigDecimal(valorTotal))
                    .build();
            
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);


            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items).build();
            
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getInitPoint();

        } catch (Exception e) {
            System.err.println("Erro ao criar link de pagamento: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao gerar link de pagamento.");
        }
    }
}
