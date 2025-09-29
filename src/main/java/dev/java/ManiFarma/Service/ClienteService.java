// package dev.java.ManiFarma.Service;
// import dev.java.ManiFarma.DTO.ClienteResponseDTO;
// import dev.java.ManiFarma.Entity.Cliente;
// import dev.java.ManiFarma.Repository.ClienteRepository;
// import org.springframework.stereotype.Service;

// import java.util.Optional;

// @Service
// public class ClienteService {

//     private final ClienteRepository clienteRepository;
//     // Remova clienteLogado se não for mais necessário para outras funcionalidades

//     public ClienteService(ClienteRepository clienteRepository) {
//         this.clienteRepository = clienteRepository;
//     }

//     // Mantenha outros métodos relacionados a clientes aqui, se houver.
//     // Exemplo: um método para buscar um cliente por ID
//     public ClienteResponseDTO findById(Long id) {
//         Optional<Cliente> optCliente = clienteRepository.findById(id);
//         if (optCliente.isEmpty()) {
//             throw new RuntimeException("Cliente não encontrado");
//         }
//         return toDTO(optCliente.get());
//     }

//     private ClienteResponseDTO toDTO(Cliente cliente) {
//         ClienteResponseDTO dto = new ClienteResponseDTO();
//         dto.setId(cliente.getId());
//         dto.setNome(cliente.getNome());
//         dto.setEmail(cliente.getEmail());
//         dto.setCpf(cliente.getCpf());
//         dto.setEndereco(cliente.getEndereco());
//         dto.setTelefone(cliente.getTelefone());
//         return dto;
//     }
// }