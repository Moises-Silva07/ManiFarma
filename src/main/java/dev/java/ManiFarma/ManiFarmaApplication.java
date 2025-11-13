package dev.java.ManiFarma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import com.mercadopago.MercadoPagoConfig;

import com.mercadopago.MercadoPagoConfig;

@SpringBootApplication
@EnableAsync
public class ManiFarmaApplication {

    public static void main(String[] args) {
        MercadoPagoConfig.setAccessToken("APP_USR-7219440132736853-102321-a481943fdb11a8e58691f1053778ba47-2943755805");

        SpringApplication.run(ManiFarmaApplication.class, args);
    }

}


// http://localhost:8080/html/login/login.html

// http://localhost:8080/html/menu_usuario/conta_usuario.html