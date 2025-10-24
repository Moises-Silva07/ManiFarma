package dev.java.ManiFarma.Controller;

import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/users" )
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- NOVO ENDPOINT ---
    @PatchMapping("/{id}/toggle-activation")
    public ResponseEntity<UserResponseDTO> toggleUserActivation(@PathVariable Long id) {
        UserResponseDTO updatedUser = userService.toggleUserActivation(id);
        return ResponseEntity.ok(updatedUser);
    }

    // --- ENDPOINTS DE DELETE AGORA DESATIVAM O USUÁRIO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateCurrentUser(Authentication authentication) {
        String userEmail = authentication.getName();
        userService.deactivateUserByEmail(userEmail);
        return ResponseEntity.noContent().build();
    }
    
    // --- Endpoints existentes ---
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserRegisterRequestDTO request) {
        UserResponseDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/senha")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String senhaAtual = body.get("senhaAtual");
        String novaSenha = body.get("novaSenha");
        userService.updatePassword(id, senhaAtual, novaSenha);
        return ResponseEntity.noContent().build();
    }
}