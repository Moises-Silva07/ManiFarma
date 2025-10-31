package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail; // Pega o e-mail das properties

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarEmailPagamento(Cliente cliente, Pedido pedido, String linkPagamento) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // DE: (Vem do application.properties)

            // --- CORREÇÃO AQUI ---
            // Agora envia para o e-mail do cliente cadastrado
            message.setTo(cliente.getEmail()); 

            // --- CORREÇÃO NO ASSUNTO ---
            message.setSubject("ManiFarma: Cotação do Pedido #" + pedido.getId());

            // Formata o valor total para BRL (R$ XX,XX)
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            String valorFormatado = currencyFormatter.format(pedido.getValorTotal() != null ? pedido.getValorTotal() : 0.0);

            // --- CORREÇÃO NO TEXTO ---
            String texto = String.format(
                    // 1º %s = cliente.getNome()
                    "Olá, %s!\n\n"
                    + // 2º %s = pedido.getId()
                    "Sua solicitação de orçamento para o pedido #%s foi processada.\n\n"
                    + // 3º %s = valorFormatado
                    "Valor total da cotação: %s\n\n"
                    + // 4º %s = linkPagamento
                    "Para aprovar e realizar o pagamento, utilize o link abaixo:\n"
                    + "%s\n\n"
                    + "Caso tenha qualquer dúvida, entre em contato conosco.\n\n"
                    + "Atenciosamente,\n"
                    + "Equipe ManiFarma",
                    
                    // Variáveis na ordem dos placeholders:
                    cliente.getNome(),     // 1º
                    pedido.getId(),        // 2º
                    valorFormatado,        // 3º
                    linkPagamento          // 4º
            );

            message.setText(texto);
            mailSender.send(message);

            System.out.println(">>> E-mail de cotação enviado com SUCESSO para: " + cliente.getEmail());

        } catch (Exception e) {
            System.err.println(">>> FALHA AO ENVIAR EMAIL para " + cliente.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}