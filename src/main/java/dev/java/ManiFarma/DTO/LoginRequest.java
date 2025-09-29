package dev.java.ManiFarma.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String senha;
}