package dev.java.ManiFarma;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import com.mercadopago.MercadoPagoConfig;

import com.mercadopago.MercadoPagoConfig;

@SpringBootApplication
public class ManiFarmaApplication {

    @Value("${MERCADOPAGO_ACCESS_TOKEN}")
    private String mpToken;

    public static void main(String[] args) {
        SpringApplication.run(ManiFarmaApplication.class, args);
    }

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(mpToken);
    }
}



// http://localhost:8080/html/login/login.html

// http://localhost:8080/html/login/cadastro_usuario.html

// http://localhost:8080/html/login/cadastro_funcionario.html