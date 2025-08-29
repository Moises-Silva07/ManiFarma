package dev.java.ManiFarma.Controller;

import dev.java.ManiFarma.DTO.PedidoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoResponseDTO;
import dev.java.ManiFarma.Service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Criar pedido
    @PostMapping("/criar")
    public ResponseEntity<?> criarPedido(@RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO dto = pedidoService.criarPedido(request);
        if (dto == null) return ResponseEntity.badRequest().body("Cliente não encontrado");
        return ResponseEntity.ok(dto);
    }

    // Listar pedidos de um cliente
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos(@PathVariable Long clienteId) {
        List<PedidoResponseDTO> pedidos = pedidoService.listarPedidosDoCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    // Atualizar pedido
    @PutMapping("/atualizar/{pedidoId}")
    public ResponseEntity<?> atualizarPedido(@PathVariable Long pedidoId, @RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO dto = pedidoService.atualizarPedido(pedidoId, request);
        if (dto == null) return ResponseEntity.badRequest().body("Pedido não encontrado");
        return ResponseEntity.ok(dto);
    }

    // Deletar pedido
    @DeleteMapping("/deletar/{pedidoId}")
    public ResponseEntity<?> deletarPedido(@PathVariable Long pedidoId) {
        boolean deletado = pedidoService.deletarPedido(pedidoId);
        if (!deletado) return ResponseEntity.badRequest().body("Pedido não encontrado");
        return ResponseEntity.ok("Pedido deletado com sucesso");
    }
}
