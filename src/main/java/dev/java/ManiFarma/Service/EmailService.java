package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Pedido;
import org.springframework.beans.factory.annotation.Value; // <-- VERIFIQUE SE TEM ESSE IMPORT
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// ... (imports)

// ... (imports)

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail; // Pega o 'eduardo.andrade.dev@gmail.com' das properties

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarEmailPagamento(Cliente cliente, Pedido pedido, String linkPagamento) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // <-- DE: eduardo.andrade.dev@gmail.com

            // --- AQUI ESTÁ A MUDANÇA PARA O TESTE ---
            // message.setTo(cliente.getEmail()); // Linha original (comente ou apague)
            message.setTo("moises0702silva@gmail.com"); // <-- PARA: eduardo.andrade.dev@gmail.com

            message.setSubject("TESTE ManiFarma: Pedido #" + pedido.getId());

            String texto = String.format(
                "Olá, comi seu pai hoje!\n\n" + // Adiciona um "TESTE" no corpo
                "ta ai seu link fdpta \n\n" +
                "Link de pagamento:\n" +
                "%s\n\n",
                cliente.getNome(),
                pedido.getId(),
                pedido.getValorTotal(),
                linkPagamento
            );

            message.setText(texto);
            mailSender.send(message);

            System.out.println(">>> E-mail de teste enviado com SUCESSO para eduardo.andrade.dev@gmail.com");

        } catch (Exception e) {
            System.err.println(">>> FALHA AO ENVIAR EMAIL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}