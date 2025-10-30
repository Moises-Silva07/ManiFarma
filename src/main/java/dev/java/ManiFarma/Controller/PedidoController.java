package dev.java.ManiFarma.Controller;

import dev.java.ManiFarma.DTO.PedidoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoResponseDTO;
import dev.java.ManiFarma.Service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Endpoint para criar um novo pedido
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO novoPedido = pedidoService.criarPedido(request);
        return new ResponseEntity<>(novoPedido, HttpStatus.CREATED);
    }

    // Endpoint para buscar todos os pedidos
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodosPedidos() {
        List<PedidoResponseDTO> pedidos = pedidoService.getAllPedidos();
        return ResponseEntity.ok(pedidos);
    }

    // Endpoint para buscar um pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPedidoPorId(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.getPedidoById(id);
        return ResponseEntity.ok(pedido);
    }

    // Endpoint para buscar todos os pedidos de um cliente específico
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPedidosPorCliente(@PathVariable Long clienteId) {
        List<PedidoResponseDTO> pedidos = pedidoService.getPedidosPorCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    // 🔹 Novo endpoint para gerar o link de cotação e enviar por e-mail
    @PostMapping("/{id}/enviar-cotacao")
    public ResponseEntity<?> enviarCotacao(@PathVariable Long id) {
        try {
            pedidoService.gerarLinkEEnviarEmail(id);
            return ResponseEntity.ok("Link de cotação gerado e e-mail enviado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar link de cotação: " + e.getMessage());
        }
    }

    // 🔹 Endpoint para atribuir um funcionário a um pedido
    @PutMapping("/{id}/atribuir")
    public ResponseEntity<?> atribuirFuncionario(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long employeeId = body.get("employeeId");
            pedidoService.atribuirFuncionario(id, employeeId);
            return ResponseEntity.ok("Funcionário atribuído com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atribuir funcionário: " + e.getMessage());
        }
    }

    // 🔹 Endpoint para alterar o status do pedido
    @PutMapping("/{id}/status")
    public ResponseEntity<?> alterarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            pedidoService.alterarStatus(id, status);
            return ResponseEntity.ok("Status atualizado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar status: " + e.getMessage());
        }
    }
}