package dev.Service;

import dev.java.ManiFarma.DTO.LoginRequest;
import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
import dev.java.ManiFarma.DTO.UserResponseDTO;
import dev.java.ManiFarma.Entity.Cliente;
import dev.java.ManiFarma.Entity.Employee;
import dev.java.ManiFarma.Entity.User;
import dev.java.ManiFarma.Repository.UserRepository;
import dev.java.ManiFarma.Service.AuthService;
import dev.java.ManiFarma.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private UserRegisterRequestDTO clienteRequestDTO;
    private UserRegisterRequestDTO employeeRequestDTO;
    private LoginRequest loginRequest;
    private Cliente cliente;
    private Employee employee;

    @BeforeEach
    void setUp() {
        // Setup Cliente Request
        clienteRequestDTO = new UserRegisterRequestDTO();
        clienteRequestDTO.setNome("João Silva");
        clienteRequestDTO.setEmail("joao@email.com");
        clienteRequestDTO.setSenha("senha123");
        clienteRequestDTO.setClient(true);
        clienteRequestDTO.setCpf("12345678900");
        clienteRequestDTO.setCep("12345-678");
        clienteRequestDTO.setRua("Rua Teste");
        clienteRequestDTO.setBairro("Bairro Teste");
        clienteRequestDTO.setCidade("São Paulo");
        clienteRequestDTO.setEstado("SP");
        clienteRequestDTO.setTelefone("11999999999");

        // Setup Employee Request
        employeeRequestDTO = new UserRegisterRequestDTO();
        employeeRequestDTO.setNome("Maria Santos");
        employeeRequestDTO.setEmail("maria@email.com");
        employeeRequestDTO.setSenha("senha123");
        employeeRequestDTO.setClient(false);
        employeeRequestDTO.setRole("FARMACEUTICO");
        employeeRequestDTO.setSalary(3000.0);
        employeeRequestDTO.setShift("MANHÃ");

        // Setup Login Request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("joao@email.com");
        loginRequest.setSenha("senha123");

        // Setup Cliente Entity
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");
        cliente.setSenha("$2a$10$encodedPassword");
        cliente.setClient(true);
        cliente.setCpf("12345678900");

        // Setup Employee Entity
        employee = new Employee();
        employee.setId(2L);
        employee.setNome("Maria Santos");
        employee.setEmail("maria@email.com");
        employee.setSenha("$2a$10$encodedPassword");
        employee.setClient(false);
        employee.setRole("FARMACEUTICO");
    }

    @Test
    void register_DeveRegistrarClienteComSucesso() {
        // Arrange
        when(userRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(false);
        when(userRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        UserResponseDTO response = authService.register(clienteRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals("João Silva", response.getNome());
        assertEquals("joao@email.com", response.getEmail());
        assertTrue(response.isClient());
        assertEquals("12345678900", response.getCpf());
        verify(userRepository).existsByEmail(clienteRequestDTO.getEmail());
        verify(userRepository).save(any(Cliente.class));
    }

    @Test
    void register_DeveRegistrarEmployeeComSucesso() {
        // Arrange
        when(userRepository.existsByEmail(employeeRequestDTO.getEmail())).thenReturn(false);
        when(userRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        UserResponseDTO response = authService.register(employeeRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals("Maria Santos", response.getNome());
        assertEquals("maria@email.com", response.getEmail());
        assertFalse(response.isClient());
        assertEquals("FARMACEUTICO", response.getRole());
        verify(userRepository).existsByEmail(employeeRequestDTO.getEmail());
        verify(userRepository).save(any(Employee.class));
    }

    @Test
    void register_DeveLancarExcecaoQuandoEmailJaExiste() {
        // Arrange
        when(userRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(clienteRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Email já cadastrado"));
        verify(userRepository).existsByEmail(clienteRequestDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_DeveAutenticarUsuarioComSucesso() {
        // Arrange
        String token = "jwt.token.here";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(cliente));
        when(jwtUtil.generateToken(loginRequest.getEmail())).thenReturn(token);

        // Act
        UserResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("João Silva", response.getNome());
        assertEquals("joao@email.com", response.getEmail());
        assertEquals(token, response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtUtil).generateToken(loginRequest.getEmail());
    }

    @Test
    void login_DeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Usuário não encontrado"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void register_DeveEncriptarSenhaAoRegistrar() {
        // Arrange
        when(userRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(false);
        when(userRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente savedCliente = invocation.getArgument(0);
            // Verifica se a senha foi encriptada (não é a senha original)
            assertNotEquals("senha123", savedCliente.getSenha());
            assertTrue(savedCliente.getSenha().startsWith("$2a$"));
            return savedCliente;
        });

        // Act
        authService.register(clienteRequestDTO);

        // Assert
        verify(userRepository).save(any(Cliente.class));
    }
}