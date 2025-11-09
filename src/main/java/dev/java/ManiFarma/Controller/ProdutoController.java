package dev.java.ManiFarma.Controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.java.ManiFarma.DTO.ProdutoRequestDTO;
import dev.java.ManiFarma.DTO.ProdutoResponseDTO;
import dev.java.ManiFarma.Service.ProdutoService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProdutoResponseDTO> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ProdutoResponseDTO buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO criado = service.criar(dto);
        return ResponseEntity
                .created(URI.create("/produtos/" + criado.getId()))
                .body(criado);
    }

    @PutMapping("/{id}")
    public ProdutoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}