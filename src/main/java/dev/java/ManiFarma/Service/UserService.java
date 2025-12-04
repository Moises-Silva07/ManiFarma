package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Employee;
import dev.java.ManiFarma.Entity.User;
import dev.java.ManiFarma.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para garantir a consistência

import java.util.List;
import java.util.UUID; // Para gerar senhas aleatórias na anonimização
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // <<< Use a interface PasswordEncoder

    // Injeta a interface no construtor
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- LÓGICA DE DESATIVAÇÃO (SOFT DELETE - REVERSÍVEL) ---
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
        user.setDisabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário com o e-mail '" + email + "' não encontrado."));
        this.deactivateUser(user.getId());
    }

    // --- LÓGICA PARA ATIVAR/DESATIVAR (TOGGLE) ---
    @Transactional
    public UserResponseDTO toggleUserActivation(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));


        if (user.isAnonymized()) {
            throw new IllegalStateException("Não é possível alterar o status de um usuário anonimizado.");
        }

        user.setDisabled(!user.isDisabled());
        User updatedUser = userRepository.save(user);
        return toUserResponseDTO(updatedUser);
    }

    @Transactional
    public void anonymizeUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));

        // Garante que a anonimização só se aplique a clientes
        if (!(user instanceof Cliente)) {
            throw new IllegalArgumentException("A anonimização de dados só é aplicável a clientes.");
        }

        Cliente cliente = (Cliente) user;

        // 1. Anonimiza os dados pessoais
        cliente.setNome("Usuário Anonimizado");
        cliente.setEmail("user_" + cliente.getId() + "@anon.com"); // E-mail único para evitar conflitos
        cliente.setSenha(passwordEncoder.encode(UUID.randomUUID().toString())); // Senha inválida e aleatória
        cliente.setCpf("000.000.000-00");
        cliente.setCep("00000-000");
        cliente.setRua("Endereço Anonimizado");
        cliente.setBairro("Bairro Anonimizado");
        cliente.setCidade("Cidade Anonimizada");
        cliente.setEstado("XX");
        cliente.setTelefone("(00) 00000-0000");

        // 2. Define as flags de estado permanente
        cliente.setDisabled(true);
        cliente.setAnonymized(true);

        userRepository.save(cliente);
    }

    @Transactional
    public void anonymizeUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário com o e-mail '" + email + "' não encontrado."));

        this.anonymizeUser(user.getId());
    }


    // --- MÉTODOS DE CONSULTA E ATUALIZAÇÃO ---

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

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRegisterRequestDTO request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));

        // Impede a atualização de um usuário anonimizado
        if (existingUser.isAnonymized()) {
            throw new IllegalStateException("Não é possível atualizar um usuário que foi anonimizado.");
        }

        // Lógica de atualização (seu código original)
        existingUser.setNome(request.getNome());
        existingUser.setEmail(request.getEmail());

        if (request.getSenha() != null && !request.getSenha().isEmpty()) {
            existingUser.setSenha(passwordEncoder.encode(request.getSenha()));
        }

        if (existingUser instanceof Cliente cliente) {
            if (request.getCpf() != null) cliente.setCpf(request.getCpf());
            if (request.getCep() != null) cliente.setCep(request.getCep());
            if (request.getRua() != null) cliente.setRua(request.getRua());
            if (request.getBairro() != null) cliente.setBairro(request.getBairro());
            if (request.getCidade() != null) cliente.setCidade(request.getCidade());
            if (request.getEstado() != null) cliente.setEstado(request.getEstado());
            if (request.getTelefone() != null) cliente.setTelefone(request.getTelefone());
        }

        if (existingUser instanceof Employee employee) {
            if (request.getRole() != null) employee.setRole(request.getRole());
            if (request.getSalary() != null) employee.setSalary(request.getSalary());
            if (request.getShift() != null) employee.setShift(request.getShift());
        }

        User updatedUser = userRepository.save(existingUser);
        return toUserResponseDTO(updatedUser);
    }

    @Transactional
    public void updatePassword(Long id, String senhaAtual, String novaSenha) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));

        if (user.isAnonymized()) {
            throw new IllegalStateException("Não é possível alterar a senha de um usuário anonimizado.");
        }

        if (!passwordEncoder.matches(senhaAtual, user.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta!");
        }

        user.setSenha(passwordEncoder.encode(novaSenha));
        userRepository.save(user);
    }

    // --- MÉTODO AUXILIAR DTO ---
    // Ajustado para incluir o novo campo 'isAnonymized'
    private UserResponseDTO toUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setNome(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setClient(user.isClient());
        dto.setDisabled(user.isDisabled());
        dto.setAnonymized(user.isAnonymized()); // Mapeia o novo campo

        if (user instanceof Cliente cliente) {
            dto.setCpf(cliente.getCpf());
            dto.setCep(cliente.getCep());
            dto.setRua(cliente.getRua());
            dto.setBairro(cliente.getBairro());
            dto.setCidade(cliente.getCidade());
            dto.setEstado(cliente.getEstado());
            dto.setTelefone(cliente.getTelefone());
        } else if (user instanceof Employee employee) {
            dto.setRole(employee.getRole());
            dto.setSalary(employee.getSalary());
            dto.setShift(employee.getShift());
        }
        return dto;
    }
}
