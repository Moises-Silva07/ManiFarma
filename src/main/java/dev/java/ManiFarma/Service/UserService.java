package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.UserRegisterRequestDTO; // Ainda necessário para updateUser
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Employee;
import dev.java.ManiFarma.Entity.User;
import dev.java.ManiFarma.Repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Removido o método createUser, pois a criação inicial é feita via AuthService

    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toUserResponseDTO(user);
    }

    // Método de atualização usando UserRegisterRequestDTO
    public UserResponseDTO updateUser(Long id, UserRegisterRequestDTO request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza todos os campos, pois o DTO de requisição é completo
        // Note: O email pode ser um campo sensível. Considere se ele deve ser atualizável aqui.
        existingUser.setNome(request.getNome());
        existingUser.setEmail(request.getEmail());
        
        // A senha só deve ser atualizada se for fornecida e não vazia
        if (request.getSenha() != null && !request.getSenha().isEmpty()) {
            existingUser.setSenha(passwordEncoder.encode(request.getSenha())); 
        }
        existingUser.setClient(request.isClient());

        // Lógica para atualizar campos específicos de Cliente/Employee
        if (existingUser instanceof Cliente) {
            Cliente cliente = (Cliente) existingUser;
            cliente.setCpf(request.getCpf());
            cliente.setEndereco(request.getEndereco());
            cliente.setTelefone(request.getTelefone());
        } else if (existingUser instanceof Employee) {
            Employee employee = (Employee) existingUser;
            employee.setRole(request.getRole());
            employee.setSalary(request.getSalary());
            employee.setShift(request.getShift());
        }

        User updatedUser = userRepository.save(existingUser);
        return toUserResponseDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        userRepository.deleteById(id);
    }

    // Método auxiliar para converter User para UserResponseDTO
    private UserResponseDTO toUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setNome(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setClient(user.isClient());

        if (user instanceof Cliente) {
            Cliente cliente = (Cliente) user;
            dto.setCpf(cliente.getCpf());
            dto.setEndereco(cliente.getEndereco());
            dto.setTelefone(cliente.getTelefone());
        } else if (user instanceof Employee) {
            Employee employee = (Employee) user;
            dto.setRole(employee.getRole());
            dto.setSalary(employee.getSalary());
            dto.setShift(employee.getShift());
        }
        return dto;
    }
}
