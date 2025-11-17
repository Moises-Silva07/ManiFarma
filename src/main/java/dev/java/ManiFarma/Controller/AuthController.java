package dev.java.ManiFarma.Controller;

import dev.java.ManiFarma.DTO.LoginRequest;
import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// IMPORTS ADICIONADOS
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import java.util.Map;
import java.util.HashMap;
// FIM DOS IMPORTS ADICIONADOS

@CrossOrigin(origins = "http://localhost:5500")
@RestController
@RequestMapping("/api/auth" )
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequestDTO request) {
        try {
            UserResponseDTO response = authService.register(request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // ERRO 400: "Email já cadastrado"
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        
        } catch (Exception e) {
            // ERRO 500: Erro de banco (ex: campo NOT NULL faltando)
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao registrar usuário.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            UserResponseDTO response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // ERRO 401: Senha incorreta
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email ou senha inválidos.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (DisabledException e) {
            // ERRO 401: Usuário desativado (seu OurUserDetailsService cuida disso)
            Map<String, String> error = new HashMap<>();
            error.put("error", "Este usuário está desativado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            
        } catch (AuthenticationException e) {
            // ERRO 401: Outro erro de autenticação (ex: usuário não encontrado)
            Map<String, String> error = new HashMap<>();
            error.put("error", "Falha na autenticação: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            
        } catch (Exception e) {
            // ERRO 500: Erro interno
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao realizar login.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}