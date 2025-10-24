package dev.java.ManiFarma.Controller;

import dev.java.ManiFarma.DTO.LoginRequest;
import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5500")
@RestController
@RequestMapping("/api/auth" )
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRegisterRequestDTO request) {
        UserResponseDTO response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody LoginRequest request) {
        UserResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}