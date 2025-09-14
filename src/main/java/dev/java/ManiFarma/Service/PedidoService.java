package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.PedidoProdutoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoProdutoResponseDTO;
import dev.java.ManiFarma.DTO.PedidoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoResponseDTO;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Employee;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Entity.PedidoProduto;
import dev.java.ManiFarma.Entity.Produto;
import dev.java.ManiFarma.Repository.ClienteRepository;
import dev.java.ManiFarma.Repository.EmployeeRepository;
import dev.java.ManiFarma.Repository.PedidoRepository;
import dev.java.ManiFarma.Repository.ProdutoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
//  28/08/2025
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final EmployeeRepository employeeRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(ProdutoRepository produtoRepository,PedidoRepository pedidoRepository, ClienteRepository clienteRepository, EmployeeRepository employeeRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.employeeRepository = employeeRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<PedidoResponseDTO> getAllOrder() {
     List<Pedido> pedidos = pedidoRepository.findAll();
     return pedidos.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PedidoResponseDTO getOrderById(Long id) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        return pedidoOpt.map(this::toDTO).orElse(null);
    }

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO request) {
        Pedido pedido = new Pedido();
        pedido.setDescricao(request.getDescricao());
        pedido.setStatus(request.getStatus());
        pedido.setReceita(request.getReceita());

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        pedido.setCliente(cliente);

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
        pedido.setEmployee(employee);

        List<PedidoProduto> itens = new ArrayList<>();
        for (PedidoProdutoRequestDTO itemDTO : request.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            PedidoProduto item = new PedidoProduto();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());

            itens.add(item);
        }
        pedido.setItens(itens);

        Pedido salvo = pedidoRepository.save(pedido);

        return toDTO(salvo);
    }

    public List<PedidoResponseDTO> listarPedidosDoCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteId(clienteId);
        return pedidos.stream().map(this::toDTO).toList();
    }
    public List<PedidoResponseDTO> getEmployeesList(Long employeeId) {
        List<Pedido> pedidos = pedidoRepository.findByEmployeeId(employeeId);
        return pedidos.stream().map(this::toDTO).toList();
    }

    public PedidoResponseDTO atualizarPedido(Long pedidoId, PedidoRequestDTO request) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isEmpty()) return null;

        Pedido pedido = pedidoOpt.get();
        pedido.setDescricao(request.getDescricao());
        pedido.setStatus(request.getStatus());
        pedido.setReceita(request.getReceita());

        pedidoRepository.save(pedido);
        return toDTO(pedido);
    }

    public boolean deletarPedido(Long pedidoId) {
        if (!pedidoRepository.existsById(pedidoId)) return false;
        pedidoRepository.deleteById(pedidoId);
        return true;
    }

     private PedidoResponseDTO toDTO(Pedido pedido) {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(pedido.getId());
        response.setDescricao(pedido.getDescricao());
        response.setStatus(pedido.getStatus());
        response.setReceita(pedido.getReceita());
        response.setClienteId(pedido.getCliente().getId());
        response.setEmployeeId(pedido.getEmployee().getId());

        List<PedidoProdutoResponseDTO> itensResponse = new ArrayList<>();
        for (PedidoProduto item : pedido.getItens()) {
            PedidoProdutoResponseDTO dto = new PedidoProdutoResponseDTO();
            dto.setProdutoId(item.getProduto().getId());
            dto.setProdutoNome(item.getProduto().getNome());
            dto.setQuantidade(item.getQuantidade());
            itensResponse.add(dto);
        }
        response.setItens(itensResponse);

        return response;
    }
}
