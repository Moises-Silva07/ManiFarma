package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Employee;
import dev.java.ManiFarma.Entity.User;
import dev.java.ManiFarma.Repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // <-- 1. IMPORTAÇÃO ADICIONADA
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));
        return toUserResponseDTO(user);
    }

    public UserResponseDTO updateUser(Long id, UserRegisterRequestDTO request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));

        existingUser.setNome(request.getNome());
        existingUser.setEmail(request.getEmail());

        if (request.getSenha() != null && !request.getSenha().isEmpty()) {
            existingUser.setSenha(passwordEncoder.encode(request.getSenha()));
        }
        existingUser.setClient(request.isClient());

        if (existingUser instanceof Cliente && request.isClient()) {
            Cliente cliente = (Cliente) existingUser;
            cliente.setCpf(request.getCpf());
            cliente.setEndereco(request.getEndereco());
            cliente.setTelefone(request.getTelefone());
        } else if (existingUser instanceof Employee && !request.isClient()) {
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
            throw new RuntimeException("Usuário não encontrado com o ID: " + id);
        }
        userRepository.deleteById(id);
    }

    // --- MÉTODO NOVO ADICIONADO ---
    /**
     * 2. MÉTODO ADICIONADO
     * Encontra um usuário pelo seu e-mail e o deleta.
     * Este método será chamado pelo endpoint que usa o token JWT para identificação.
     * @param email O e-mail do usuário a ser deletado.
     */
    public void deleteUserByEmail(String email) {
        // Busca o usuário pelo e-mail. Se não encontrar, lança uma exceção clara.
        User userToDelete = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário com o e-mail '" + email + "' não encontrado."));

        // Reutiliza o método deleteUser(id) para manter a lógica centralizada.
        this.deleteUser(userToDelete.getId());
    }
    // --- FIM DO MÉTODO NOVO ---

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
