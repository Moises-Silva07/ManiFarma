package dev.java.ManiFarma.Controller;

import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Service.UserService;

// IMPORTS ADICIONADOS
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;
// FIM DOS IMPORTS

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- NOVO ENDPOINT ---
    @PatchMapping("/{id}/toggle-activation")
    public ResponseEntity<?> toggleUserActivation(@PathVariable Long id) {
        try {
            UserResponseDTO updatedUser = userService.toggleUserActivation(id);
            return ResponseEntity.ok(updatedUser);

        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao ativar/desativar usuário.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // --- ENDPOINTS DE DELETE AGORA DESATIVAM O USUÁRIO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao desativar usuário.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deactivateCurrentUser(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            userService.deactivateUserByEmail(userEmail);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao desativar usuário atual.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/me/anonymize")
    public ResponseEntity<?> anonymizeCurrentUser(Authentication authentication) {
        try {
            String userEmail = authentication.getName(); // Pega o email do token
            userService.anonymizeUserByEmail(userEmail);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



    @DeleteMapping("/{id}/anonymize")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> anonymizeUser(@PathVariable Long id) {
        try {
            userService.anonymizeUser(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            // Captura o erro se tentarem anonimizar um funcionarios
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }

    // --- Endpoints existentes ---
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        List<UserResponseDTO> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponseDTO user = userService.findUserById(id);
            return ResponseEntity.ok(user);

        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao buscar usuário.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRegisterRequestDTO request) {
        try {
            UserResponseDTO updatedUser = userService.updateUser(id, request);
            return ResponseEntity.ok(updatedUser);
            
        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao atualizar usuário.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}/senha")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String senhaAtual = body.get("senhaAtual");
            String novaSenha = body.get("novaSenha");
            
            userService.updatePassword(id, senhaAtual, novaSenha);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Senha atualizada com sucesso!");
            return ResponseEntity.ok(successResponse);

            // TRATAMENTO DE ERROS

        } catch (EntityNotFoundException e) {
            // ERRO 404: Usuário não encontrado
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (IllegalArgumentException e) {
            // ERRO 400: Senha atual incorreta
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        
        } catch (Exception e) {
            // ERRO 500: Outros erros
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao atualizar senha.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}