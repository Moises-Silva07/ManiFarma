package dev.Service;

import dev.java.ManiFarma.DTO.ProdutoRequestDTO;
import dev.java.ManiFarma.DTO.ProdutoResponseDTO;
import dev.java.ManiFarma.Entity.Produto;
import dev.java.ManiFarma.Repository.ProdutoRepository;
import dev.java.ManiFarma.Service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;
    private ProdutoRequestDTO produtoRequestDTO;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Paracetamol");
        produto.setPreco(15.50);

        produtoRequestDTO = new ProdutoRequestDTO();
        produtoRequestDTO.setNome("Ibuprofeno");
        produtoRequestDTO.setPreco(20.00);
    }

    @Test
    void listarTodos_DeveRetornarListaDeProdutos() {
        // Arrange
        Produto produto2 = new Produto();
        produto2.setId(2L);
        produto2.setNome("Dipirona");
        produto2.setPreco(10.00);

        when(produtoRepository.findAll()).thenReturn(Arrays.asList(produto, produto2));

        // Act
        List<ProdutoResponseDTO> resultado = produtoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Paracetamol", resultado.get(0).getNome());
        assertEquals("Dipirona", resultado.get(1).getNome());
        verify(produtoRepository).findAll();
    }

    @Test
    void listarTodos_DeveRetornarListaVaziaQuandoNaoHaProdutos() {
        // Arrange
        when(produtoRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ProdutoResponseDTO> resultado = produtoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(produtoRepository).findAll();
    }

    @Test
    void buscarPorId_DeveRetornarProdutoQuandoExiste() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        // Act
        ProdutoResponseDTO resultado = produtoService.buscarPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Paracetamol", resultado.getNome());
        assertEquals(15.50, resultado.getPreco());
        verify(produtoRepository).findById(1L);
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoProdutoNaoExiste() {
        // Arrange
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            produtoService.buscarPorId(99L);
        });

        assertTrue(exception.getMessage().contains("Produto não encontrado"));
        verify(produtoRepository).findById(99L);
    }

    @Test
    void criar_DeveCriarProdutoComSucesso() {
        // Arrange
        Produto novoProduto = new Produto();
        novoProduto.setId(3L);
        novoProduto.setNome(produtoRequestDTO.getNome());
        novoProduto.setPreco(produtoRequestDTO.getPreco());

        when(produtoRepository.save(any(Produto.class))).thenReturn(novoProduto);

        // Act
        ProdutoResponseDTO resultado = produtoService.criar(produtoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(3L, resultado.getId());
        assertEquals("Ibuprofeno", resultado.getNome());
        assertEquals(20.00, resultado.getPreco());
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void atualizar_DeveAtualizarProdutoExistente() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        ProdutoResponseDTO resultado = produtoService.atualizar(1L, produtoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Ibuprofeno", resultado.getNome());
        assertEquals(20.00, resultado.getPreco());
        verify(produtoRepository).findById(1L);
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void atualizar_DeveLancarExcecaoQuandoProdutoNaoExiste() {
        // Arrange
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            produtoService.atualizar(99L, produtoRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Produto não encontrado"));
        verify(produtoRepository).findById(99L);
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void deletar_DeveDeletarProdutoExistente() {
        // Arrange
        when(produtoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(produtoRepository).deleteById(1L);

        // Act
        assertDoesNotThrow(() -> produtoService.deletar(1L));

        // Assert
        verify(produtoRepository).existsById(1L);
        verify(produtoRepository).deleteById(1L);
    }

    @Test
    void deletar_DeveLancarExcecaoQuandoProdutoNaoExiste() {
        // Arrange
        when(produtoRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            produtoService.deletar(99L);
        });

        assertTrue(exception.getMessage().contains("Produto não encontrado"));
        verify(produtoRepository).existsById(99L);
        verify(produtoRepository, never()).deleteById(99L);
    }
}