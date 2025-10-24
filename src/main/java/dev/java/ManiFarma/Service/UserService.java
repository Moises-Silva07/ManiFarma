package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Employee;
import dev.java.ManiFarma.Entity.User;
import dev.java.ManiFarma.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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

    // --- LÓGICA DE DESATIVAÇÃO (SOFT DELETE) ---
    // O método que antes deletava, agora desativa o usuário.
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
        user.setDisabled(true); // Altera a flag
        userRepository.save(user);
    }

    public void deactivateUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário com o e-mail '" + email + "' não encontrado."));
        this.deactivateUser(user.getId());
    }
    
    // --- NOVO MÉTODO PARA ATIVAR/DESATIVAR ---
    public UserResponseDTO toggleUserActivation(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
        
        user.setDisabled(!user.isDisabled()); // Inverte o valor da flag
        User updatedUser = userRepository.save(user);
        
        return toUserResponseDTO(updatedUser);
    }

    // O método toUserResponseDTO agora precisa mapear a nova flag
    private UserResponseDTO toUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setNome(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setClient(user.isClient());
        dto.setDisabled(user.isDisabled()); // Mapeia o novo campo

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
    
    // --- MÉTODOS EXISTENTES (findAll, findById, updateUser, etc.) ---
    // (O resto dos seus métodos como findAllUsers, findUserById, updateUser, etc., permanecem aqui sem alterações)

    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
        return toUserResponseDTO(user);
    }

    public UserResponseDTO updateUser(Long id, UserRegisterRequestDTO request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));

        existingUser.setNome(request.getNome());
        existingUser.setEmail(request.getEmail());

        if (request.getSenha() != null && !request.getSenha().isEmpty()) {
            existingUser.setSenha(passwordEncoder.encode(request.getSenha()));
        }

        if (existingUser instanceof Cliente) {
            Cliente cliente = (Cliente) existingUser;
            if (request.getCpf() != null) cliente.setCpf(request.getCpf());
            if (request.getEndereco() != null) cliente.setEndereco(request.getEndereco());
            if (request.getTelefone() != null) cliente.setTelefone(request.getTelefone());
        }

        if (existingUser instanceof Employee) {
            Employee employee = (Employee) existingUser;
            if (request.getRole() != null) employee.setRole(request.getRole());
            if (request.getSalary() != null) employee.setSalary(request.getSalary());
            if (request.getShift() != null) employee.setShift(request.getShift());
        }

        User updatedUser = userRepository.save(existingUser);
        return toUserResponseDTO(updatedUser);
    }

    public void updatePassword(Long id, String senhaAtual, String novaSenha) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));

        if (!passwordEncoder.matches(senhaAtual, user.getSenha())) {
            throw new RuntimeException("Senha atual incorreta!");
        }

        user.setSenha(passwordEncoder.encode(novaSenha));
        userRepository.save(user);
    }
}