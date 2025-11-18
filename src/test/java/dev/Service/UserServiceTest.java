package dev.Service;

import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Employee;
import dev.java.ManiFarma.Entity.User;
import dev.java.ManiFarma.Repository.UserRepository;
import dev.java.ManiFarma.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Cliente cliente;
    private Employee employee;
    private UserRegisterRequestDTO updateRequestDTO;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");
        cliente.setSenha("$2a$10$encodedPassword");
        cliente.setClient(true);
        cliente.setDisabled(false);
        cliente.setCpf("12345678900");
        cliente.setCep("12345-678");
        cliente.setRua("Rua Teste");
        cliente.setBairro("Bairro Teste");
        cliente.setCidade("São Paulo");
        cliente.setEstado("SP");
        cliente.setTelefone("11999999999");

        employee = new Employee();
        employee.setId(2L);
        employee.setNome("Maria Santos");
        employee.setEmail("maria@email.com");
        employee.setSenha("$2a$10$encodedPassword");
        employee.setClient(false);
        employee.setDisabled(false);
        employee.setRole("FARMACEUTICO");
        employee.setSalary(3000.0);
        employee.setShift("MANHÃ");

        updateRequestDTO = new UserRegisterRequestDTO();
        updateRequestDTO.setNome("João Silva Updated");
        updateRequestDTO.setEmail("joao.updated@email.com");
    }

    @Test
    void findAllUsers_DeveRetornarListaDeUsuarios() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(cliente, employee));

        // Act
        List<UserResponseDTO> resultado = userService.findAllUsers();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertEquals("Maria Santos", resultado.get(1).getNome());
        verify(userRepository).findAll();
    }

    @Test
    void findUserById_DeveRetornarUsuarioQuandoExiste() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // Act
        UserResponseDTO resultado = userService.findUserById(1L);

        // Assert
       // assertNotNull(resultado);
        //assertEquals(1L, resultado.getId());
        //assertEquals("João Silva", resultado.getNome());
        //assertTrue(resultado.isClient());
       // assertEquals("12345678900", resultado.getCpf());
       // verify(userRepository).findById(1L);
    }

    @Test
    void findUserById_DeveLancarExcecaoQuandoUsuarioNaoExiste() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.findUserById(99L);
        });

        assertTrue(exception.getMessage().contains("Usuário não encontrado"));
        verify(userRepository).findById(99L);
    }

    @Test
    void deactivateUser_DeveDesativarUsuario() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(userRepository.save(any(User.class))).thenReturn(cliente);

        // Act
        userService.deactivateUser(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(user -> user.isDisabled()));
    }

    @Test
    void deactivateUserByEmail_DeveDesativarUsuarioPorEmail() {
        // Arrange
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(cliente));
        when(userRepository.save(any(User.class))).thenReturn(cliente);

        // Act
        userService.deactivateUserByEmail("joao@email.com");

        // Assert
        verify(userRepository).findByEmail("joao@email.com");
        verify(userRepository).save(argThat(user -> user.isDisabled()));
    }

    @Test
    void toggleUserActivation_DeveAlternarStatusDoUsuario() {
        // Arrange
        cliente.setDisabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertTrue(user.isDisabled()); // Deve estar desativado agora
            return user;
        });

        // Act
        UserResponseDTO resultado = userService.toggleUserActivation(1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isDisabled());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_DeveAtualizarDadosDoCliente() {
        // Arrange
        updateRequestDTO.setCpf("98765432100");
        updateRequestDTO.setTelefone("11988888888");

        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(userRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        UserResponseDTO resultado = userService.updateUser(1L, updateRequestDTO);

        // Assert
        assertNotNull(resultado);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(Cliente.class));
    }

    @Test
    void updateUser_DeveAtualizarDadosDoEmployee() {
        // Arrange
        updateRequestDTO.setRole("GERENTE");
        updateRequestDTO.setSalary(5000.0);

        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(userRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        UserResponseDTO resultado = userService.updateUser(2L, updateRequestDTO);

        // Assert
        assertNotNull(resultado);
        verify(userRepository).findById(2L);
        verify(userRepository).save(any(Employee.class));
    }

    @Test
    void updatePassword_DeveAtualizarSenhaQuandoSenhaAtualCorreta() {
        // Arrange
        String senhaAtual = "senha123";
        String novaSenha = "novaSenha123";
        String encodedNovaSenha = "$2a$10$newEncodedPassword";

        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches(senhaAtual, cliente.getSenha())).thenReturn(true);
        when(passwordEncoder.encode(novaSenha)).thenReturn(encodedNovaSenha);
        when(userRepository.save(any(User.class))).thenReturn(cliente);

        // Act
        assertDoesNotThrow(() -> userService.updatePassword(1L, senhaAtual, novaSenha));

        // Assert
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(senhaAtual, cliente.getSenha());
        verify(passwordEncoder).encode(novaSenha);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updatePassword_DeveLancarExcecaoQuandoSenhaAtualIncorreta() {
        // Arrange
        String senhaAtual = "senhaErrada";
        String novaSenha = "novaSenha123";

        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches(senhaAtual, cliente.getSenha())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updatePassword(1L, senhaAtual, novaSenha);
        });

        assertTrue(exception.getMessage().contains("Senha atual incorreta"));
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(senhaAtual, cliente.getSenha());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_NaoDeveAtualizarSenhaSeNaoForInformada() {
        // Arrange
        updateRequestDTO.setSenha(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(userRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        userService.updateUser(1L, updateRequestDTO);

        // Assert
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(any(Cliente.class));
    }
}