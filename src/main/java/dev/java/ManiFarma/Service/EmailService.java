package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Pedido;
import org.springframework.beans.factory.annotation.Value; // <-- VERIFIQUE SE TEM ESSE IMPORT
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.NumberFormat; // Para formatar o valor
import java.util.Locale;       // Para formatar o valor

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail; 

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarEmailPagamento(Cliente cliente, Pedido pedido, String linkPagamento) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); 


            message.setTo(cliente.getEmail());
            

            message.setSubject("ManiFarma: Cotação do Pedido #" + pedido.getId());


            NumberFormat formatadorMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            String valorFormatado = formatadorMoeda.format(pedido.getValorTotal());


            String texto = String.format(
                    // 1º %s = cliente.getNome()
                    "Olá, %s!\n\n"
                    + // 2º %s = pedido.getId()
                    "Sua cotação para o pedido #%s está pronta.\n\n"
                    + // 3º %s = valorFormatado
                    "Valor total: %s\n\n"
                    + // 4º %s = linkPagamento
                    "Para realizar o pagamento, acesse o link abaixo:\n"
                    + "%s\n\n"
                    + "Atenciosamente,\n"
                    + "Equipe ManiFarma",
                    // Variáveis na ordem:
                    cliente.getNome(),     // 1º
                    pedido.getId(),        // 2º
                    valorFormatado,        // 3º
                    linkPagamento          // 4º
            );

            message.setText(texto);
            mailSender.send(message);

            System.out.println(">>> E-mail de cotação enviado com SUCESSO para " + cliente.getEmail());

        } catch (Exception e) {
            System.err.println(">>> FALHA AO ENVIAR EMAIL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}