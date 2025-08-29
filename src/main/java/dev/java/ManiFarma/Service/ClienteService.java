package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.ClienteResponseDTO;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private Cliente clienteLogado; // simples, para teste

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteResponseDTO cadastrar(Cliente cliente) {
        clienteRepository.save(cliente);
        return toDTO(cliente);
    }

    public ClienteResponseDTO login(String email, String senha) {
        Optional<Cliente> opt = clienteRepository.findByEmailAndSenha(email, senha);
        if (opt.isPresent()) {
            clienteLogado = opt.get();
            return toDTO(clienteLogado);
        }
        return null;
    }

    public Cliente getClienteLogado() {
        return clienteLogado;
    }

    private ClienteResponseDTO toDTO(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setEmail(cliente.getEmail());
        dto.setCpf(cliente.getCpf());
        dto.setEndereco(cliente.getEndereco());
        dto.setTelefone(cliente.getTelefone());
        return dto;
    }
}
