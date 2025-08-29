package dev.java.ManiFarma.Controller;

import dev.java.ManiFarma.DTO.ClienteRequestDTO;
import dev.java.ManiFarma.DTO.ClienteResponseDTO;
import dev.java.ManiFarma.DTO.LoginRequest;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // ----------------------------
    // Cadastro de cliente
    // ----------------------------
    @PostMapping("/cadastro")
    public ResponseEntity<ClienteResponseDTO> cadastrar(@RequestBody ClienteRequestDTO request) {
        // Monta a entidade a partir do DTO
        Cliente cliente = new Cliente();
        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());
        cliente.setCpf(request.getCpf());
        cliente.setEndereco(request.getEndereco());
        cliente.setTelefone(request.getTelefone());
        cliente.setSenha(request.getSenha()); // senha em texto, pode ser criptografada no service

        ClienteResponseDTO dto = clienteService.cadastrar(cliente);
        return ResponseEntity.ok(dto);
    }

    // ----------------------------
    // Login de cliente
    // ----------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        ClienteResponseDTO dto = clienteService.login(request.getEmail(), request.getSenha());
        if (dto == null) {
            return ResponseEntity.badRequest().body("Login inválido");
        }
        return ResponseEntity.ok(dto);
    }


}
