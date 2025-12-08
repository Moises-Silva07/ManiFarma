package dev.java.ManiFarma.Controller;

import dev.java.ManiFarma.DTO.PedidoResponseDTO;
import dev.java.ManiFarma.Entity.StatusPedido;
import dev.java.ManiFarma.Service.PedidoService;
import dev.java.ManiFarma.Repository.PedidoRepository;
import dev.java.ManiFarma.Entity.Pedido;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.annotation.Transactional; // @Transactionaly;

import dev.java.ManiFarma.DTO.PedidoProdutoRequestDTO; // seu DTO

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*") // Ajuste conforme necessário
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;

    public PedidoController(PedidoService pedidoService, PedidoRepository pedidoRepository) {
        this.pedidoService = pedidoService;
        this.pedidoRepository = pedidoRepository;
    }


    // CRIAR PEDIDO - Cliente envia apenas descrição + imagem
    // Este método já possui um bom tratamento try...catch
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> criarPedido(
            @RequestParam("descricao") String descricao,
            @RequestParam("clienteId") Long clienteId,
            @RequestPart("receita") MultipartFile receita
    ) {
        try {
            PedidoResponseDTO novoPedido = pedidoService.criarPedidoMultipart(
                    descricao,
                    StatusPedido.PENDENTE,
                    clienteId,
                    null, // Sem funcionário inicialmente
                    receita
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido criado com sucesso!");
            response.put("pedido", novoPedido);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Erros de validação (tipo de arquivo, tamanho, etc)
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (EntityNotFoundException e) {
            // Cliente não encontrado
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            // Erro interno
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Erro ao criar pedido: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    //  VISUALIZAR/BAIXAR IMAGEM DA RECEITA
    // Este método já possui um bom tratamento try...catch
    @GetMapping("/{id}/receita")
    public ResponseEntity<?> visualizarReceita(@PathVariable Long id) {
        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

            if (pedido.getCaminhoReceita() == null || pedido.getCaminhoReceita().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Este pedido não possui imagem de receita");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Path caminhoArquivo = Paths.get(pedido.getCaminhoReceita());
            Resource resource = new UrlResource(caminhoArquivo.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Detecta o tipo da imagem pelo nome do arquivo
                String fileName = caminhoArquivo.getFileName().toString().toLowerCase();
                MediaType mediaType = MediaType.IMAGE_JPEG; // Padrão

                if (fileName.endsWith(".png")) {
                    mediaType = MediaType.IMAGE_PNG;
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    mediaType = MediaType.IMAGE_JPEG;
                }

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header("Content-Disposition", "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Arquivo de imagem não encontrado no servidor");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao carregar imagem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    // LISTAR TODOS OS PEDIDOS
    // Este método já possui um bom tratamento try...catch
    @GetMapping
    public ResponseEntity<?> listarTodosPedidos() {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getAllPedidos();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao listar todos os pedidos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    //  BUSCAR PEDIDO POR ID
    // Este método já possui um bom tratamento try...catch
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPedidoPorId(@PathVariable Long id) {
        try {
            PedidoResponseDTO pedido = pedidoService.getPedidoById(id);
            return ResponseEntity.ok(pedido);

        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao buscar pedido: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    //  BUSCAR PEDIDOS POR CLIENTE
    // Este método já possui um bom tratamento try...catch
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> buscarPedidosPorCliente(@PathVariable Long clienteId) {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosPorCliente(clienteId);
            return ResponseEntity.ok(pedidos);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao buscar pedidos do cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==========================================================
    // 2. NOVO ENDPOINT ADICIONADO AQUI
    // ==========================================================
    @GetMapping("/funcionario/{employeeId}")
    public ResponseEntity<?> buscarPedidosPorFuncionario(@PathVariable Long employeeId) {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosPorFuncionario(employeeId);
            return ResponseEntity.ok(pedidos);

        } catch (EntityNotFoundException e) { // Captura se o ID do funcionário não existir
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao buscar pedidos do funcionário: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    //  ENVIAR COTAÇÃO - Funcionário gera link e envia email
    // Este método já possui um bom tratamento try...catch
    @PostMapping("/{id}/enviar-cotacao")
    public ResponseEntity<?> enviarCotacao(@PathVariable Long id) {
        try {
            pedidoService.gerarLinkEEnviarEmail(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Link de cotação gerado e e-mail enviado com sucesso!");
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (RuntimeException e) { // Captura erros de negócio (ex: pedido sem valor)
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Erro ao gerar link de cotação: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    //  ATRIBUIR FUNCIONÁRIO AO PEDIDO
    // Este método já possui um bom tratamento try...catch
    @PutMapping("/{id}/atribuir")
    public ResponseEntity<?> atribuirFuncionario(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long employeeId = body.get("employeeId");

            if (employeeId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "O campo 'employeeId' é obrigatório");
                return ResponseEntity.badRequest().body(error);
            }

            pedidoService.atribuirFuncionario(id, employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Funcionário atribuído com sucesso!");
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Erro ao atribuir funcionário: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<?> alterarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            String senha = body.get("senha"); // <-- 1. ADICIONADO: Pega a senha do corpo da requisição

            if (status == null || status.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "O campo 'status' é obrigatório");
                return ResponseEntity.badRequest().body(error);
            }
            pedidoService.alterarStatus(id, status, senha);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Status atualizado com sucesso!");
            response.put("novoStatus", status.toUpperCase());
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (SecurityException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            // Retorna 403 Forbidden, o código correto para falha de autorização
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Erro ao atualizar status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Endpoint para listar pedidos pelo status
    // Este método já possui um bom tratamento try...catch
    @GetMapping("/status/{status}")
    public ResponseEntity<?> listarPedidosPorStatus(@PathVariable String status) {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosPorStatus(status);
            return ResponseEntity.ok(pedidos);
        
        } catch (IllegalArgumentException e) { // Captura se o status for inválido (ex: "ABCDE")
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao listar pedidos por status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Endpoint para puxar o id pedido para adicionar o item
    // Este método já possui um bom tratamento try...catch
    @PostMapping("/{pedidoId}/itens")
    @Transactional
    public ResponseEntity<?> adicionarItensAoPedido(
            @PathVariable Long pedidoId,
            @RequestBody List<PedidoProdutoRequestDTO> itens) {
        
        try {
            PedidoResponseDTO atualizado = pedidoService.adicionarItensAoPedido(pedidoId, itens);
            return ResponseEntity.ok(atualizado);

        } catch (EntityNotFoundException e) { // Captura se o Pedido ID ou algum Produto ID não existir
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao adicionar itens ao pedido: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}