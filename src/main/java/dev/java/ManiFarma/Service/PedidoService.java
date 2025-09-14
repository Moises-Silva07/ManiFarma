package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.PedidoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoResponseDTO;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Employee;
import dev.java.ManiFarma.Entity.Pedido;
import dev.java.ManiFarma.Repository.ClienteRepository;
import dev.java.ManiFarma.Repository.EmployeeRepository;
import dev.java.ManiFarma.Repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
//  28/08/2025
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final EmployeeRepository employeeRepository;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository, EmployeeRepository employeeRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<PedidoResponseDTO> getAllOrder() {
     List<Pedido> pedidos = pedidoRepository.findAll();
     return pedidos.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PedidoResponseDTO getOrderById(Long id) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        return pedidoOpt.map(this::toDTO).orElse(null);
    }

    public PedidoResponseDTO criarPedido(PedidoRequestDTO request) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(request.getClienteId());
        Optional<Employee> employeeOpt = employeeRepository.findById(request.getEmployeeId());
        if (clienteOpt.isEmpty()) return null;
        if (employeeOpt.isEmpty()) return null;

        Pedido pedido = new Pedido();
        pedido.setCliente(clienteOpt.get());
        pedido.setEmployee(employeeOpt.get());
        pedido.setDescricao(request.getDescricao());
        pedido.setStatus(request.getStatus());
        pedido.setReceita(request.getReceita());

        pedidoRepository.save(pedido);
        return toDTO(pedido);
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
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setClienteId(pedido.getCliente().getId());
        dto.setEmployeeId(pedido.getEmployee().getId());
        dto.setDescricao(pedido.getDescricao());
        dto.setStatus(pedido.getStatus());
        dto.setReceita(pedido.getReceita());
        return dto;
    }
}
