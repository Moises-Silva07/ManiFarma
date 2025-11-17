package dev.java.ManiFarma.Controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.java.ManiFarma.DTO.ProdutoRequestDTO;
import dev.java.ManiFarma.DTO.ProdutoResponseDTO;
import dev.java.ManiFarma.Service.ProdutoService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
// ADICIONE ESTES IMPORTS
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;
// FIM DOS IMPORTS ADICIONADOS

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// ... (outros imports)

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProdutoResponseDTO> listar() {
        // GET all geralmente não precisa de try-catch,
        // pois retorna lista vazia, o que é ok.
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        try {
            ProdutoResponseDTO produto = service.buscarPorId(id);
            return ResponseEntity.ok(produto);
        
        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao buscar produto.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        // Criar geralmente não tem erro 404, então podemos manter simples.
        // A validação @Valid já retorna 400 automaticamente se o DTO falhar.
        ProdutoResponseDTO criado = service.criar(dto);
        return ResponseEntity
                .created(URI.create("/produtos/" + criado.getId()))
                .body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDTO dto) {
        try {
            ProdutoResponseDTO atualizado = service.atualizar(id, dto);
            return ResponseEntity.ok(atualizado);

        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao atualizar produto.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            service.deletar(id);
            return ResponseEntity.noContent().build(); // 204 No Content

        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno ao deletar produto.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}