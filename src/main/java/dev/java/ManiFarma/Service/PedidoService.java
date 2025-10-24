package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.PedidoProdutoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoProdutoResponseDTO;
import dev.java.ManiFarma.DTO.PedidoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoResponseDTO;
import dev.java.ManiFarma.Entity.*;
import dev.java.ManiFarma.Repository.ClienteRepository;
import dev.java.ManiFarma.Repository.EmployeeRepository;
import dev.java.ManiFarma.Repository.PedidoRepository;
import dev.java.ManiFarma.Repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final EmployeeRepository employeeRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository, EmployeeRepository employeeRepository, ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.employeeRepository = employeeRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO request) {
        // 1. Busca o cliente. Se não existir, lança uma exceção clara.
        Cliente cliente = (Cliente) clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + request.getClienteId()));

        // 2. Cria a entidade Pedido
        Pedido pedido = new Pedido();
        pedido.setDescricao(request.getDescricao());
        pedido.setStatus(StatusPedido.PENDENTE); // Define o status inicial como pendente
        pedido.setReceita(request.getReceita());
        pedido.setCliente(cliente);

        // 3. Associa o funcionário apenas se um employeeId for fornecido
        if (request.getEmployeeId() != null) {
            Employee employee = (Employee) employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado com ID: " + request.getEmployeeId()));
            pedido.setEmployee(employee);
        }

        // 4. Processa os itens do pedido
        if (request.getItens() != null && !request.getItens().isEmpty()) {
            List<PedidoProduto> itens = new ArrayList<>();
            for (PedidoProdutoRequestDTO itemDTO : request.getItens()) {
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + itemDTO.getProdutoId()));

                PedidoProduto item = new PedidoProduto();
                item.setPedido(pedido);
                item.setProduto(produto);
                item.setQuantidade(itemDTO.getQuantidade());
                itens.add(item);
            }
            pedido.setItens(itens);
        }

        // 5. Salva o pedido e seus itens no banco de dados
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // 6. Retorna o DTO de resposta
        return toDTO(pedidoSalvo);
    }

    public List<PedidoResponseDTO> getAllPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO getPedidoById(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
        return toDTO(pedido);
    }
    
    public List<PedidoResponseDTO> getPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    // Converte a entidade Pedido para um DTO de resposta de forma segura
    private PedidoResponseDTO toDTO(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setDescricao(pedido.getDescricao());
        dto.setStatus(pedido.getStatus());
        dto.setReceita(pedido.getReceita());

        // Associa IDs de forma segura, verificando se não são nulos
        if (pedido.getCliente() != null) {
            dto.setClienteId(pedido.getCliente().getId());
        }
        if (pedido.getEmployee() != null) {
            dto.setEmployeeId(pedido.getEmployee().getId());
        }

        // Mapeia os itens do pedido para DTOs
        if (pedido.getItens() != null) {
            dto.setItens(pedido.getItens().stream().map(item -> {
                PedidoProdutoResponseDTO itemDto = new PedidoProdutoResponseDTO();
                itemDto.setProdutoId(item.getProduto().getId());
                itemDto.setProdutoNome(item.getProduto().getNome());
                itemDto.setQuantidade(item.getQuantidade());
                return itemDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}