package dev.java.ManiFarma.Service;


import org.springframework.stereotype.Service;

import dev.java.ManiFarma.DTO.ProdutoRequestDTO;
import dev.java.ManiFarma.DTO.ProdutoResponseDTO;
import dev.java.ManiFarma.Entity.Produto;
import dev.java.ManiFarma.Repository.ProdutoRepository;

import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
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
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        entity.setNome(dto.getNome());
        entity.setPreco(dto.getPreco());
        Produto atualizado = repository.save(entity);
        return new ProdutoResponseDTO(atualizado.getId(), atualizado.getNome(), atualizado.getPreco());
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado");
        }
        repository.deleteById(id);
    }
}