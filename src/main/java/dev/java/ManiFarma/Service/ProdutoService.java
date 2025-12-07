package dev.java.ManiFarma.Service;


import org.springframework.stereotype.Service;

import dev.java.ManiFarma.DTO.ProdutoRequestDTO;
import dev.java.ManiFarma.DTO.ProdutoResponseDTO;
import dev.java.ManiFarma.Entity.Produto;
import dev.java.ManiFarma.Entity.Unidade;
import dev.java.ManiFarma.Repository.ProdutoRepository;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public List<ProdutoResponseDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(p -> {
                    String unidade = (p.getUnidade() != null)
                            ? p.getUnidade().name()
                            : "MG";

                    return new ProdutoResponseDTO(
                            p.getId(),
                            p.getNome(),
                            p.getPreco(),
                            unidade
                    );
                })
                .collect(Collectors.toList());
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));

        String unidade = (entity.getUnidade() != null)
                ? entity.getUnidade().name()
                : "MG";

        return new ProdutoResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getPreco(),
                unidade
        );
    }

    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        System.out.println("Nome: " + dto.getNome());
        System.out.println("Preço: " + dto.getPreco());  // Verifique o valor do preço aqui

        Produto entity = new Produto();
        entity.setNome(dto.getNome());
        entity.setPreco(dto.getPreco());

        // Converte string para enum, default MG
        Unidade unidade = Unidade.MG;
        if (dto.getUnidade() != null && !dto.getUnidade().isBlank()) {
            unidade = Unidade.valueOf(dto.getUnidade().toUpperCase());
        }
        entity.setUnidade(unidade);

        Produto salvo = repository.save(entity);

        return new ProdutoResponseDTO(
                salvo.getId(),
                salvo.getNome(),
                salvo.getPreco(),
                salvo.getUnidade() != null ? salvo.getUnidade().name() : "MG"
        );
    }

    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));

        entity.setNome(dto.getNome());
        entity.setPreco(dto.getPreco());

        if (dto.getUnidade() != null && !dto.getUnidade().isBlank()) {
            entity.setUnidade(Unidade.valueOf(dto.getUnidade().toUpperCase()));
        }

        Produto atualizado = repository.save(entity);

        return new ProdutoResponseDTO(
                atualizado.getId(),
                atualizado.getNome(),
                atualizado.getPreco(),
                atualizado.getUnidade() != null ? atualizado.getUnidade().name() : "MG"
        );
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            // CORRIGIDO:
            throw new EntityNotFoundException("Produto não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }
}