package dev.java.ManiFarma.Controller;

import dev.java.ManiFarma.DTO.PedidoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoResponseDTO;
import dev.java.ManiFarma.Service.PedidoService;
import org.springframework.http.HttpStatus;
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
}