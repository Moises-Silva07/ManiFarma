package dev.java.ManiFarma.Service;


import org.springframework.stereotype.Service;

import dev.java.ManiFarma.DTO.ProdutoRequestDTO;
import dev.java.ManiFarma.DTO.ProdutoResponseDTO;
import dev.java.ManiFarma.Entity.Produto;
import dev.java.ManiFarma.Repository.ProdutoRepository;

import java.util.List;
import java.util.stream.Collectors;
// ADICIONE ESTE IMPORT
import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
// ... (outros imports)
@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public List<ProdutoResponseDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(p -> new ProdutoResponseDTO(p.getId(), p.getNome(), p.getPreco()))
                .collect(Collectors.toList());
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto entity = repository.findById(id)
                // CORRIGIDO:
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));
        return new ProdutoResponseDTO(entity.getId(), entity.getNome(), entity.getPreco());
    }

    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Produto entity = new Produto();
        entity.setNome(dto.getNome());
        entity.setPreco(dto.getPreco());
        Produto salvo = repository.save(entity);
        return new ProdutoResponseDTO(salvo.getId(), salvo.getNome(), salvo.getPreco());
    }

    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto entity = repository.findById(id)
                // CORRIGIDO:
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));
        entity.setNome(dto.getNome());
        entity.setPreco(dto.getPreco());
        Produto atualizado = repository.save(entity);
        return new ProdutoResponseDTO(atualizado.getId(), atualizado.getNome(), atualizado.getPreco());
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            // CORRIGIDO:
            throw new EntityNotFoundException("Produto não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }
}