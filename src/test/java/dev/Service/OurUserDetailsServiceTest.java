package dev.Service;

import dev.java.ManiFarma.Entity.User;
import dev.java.ManiFarma.Repository.UserRepository;
import dev.java.ManiFarma.Service.OurUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OurUserDetailsServiceTest { // <--- NOME CORRIGIDO

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OurUserDetailsService ourUserDetailsService;

    private User user;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(userEmail);
        user.setSenha("password123");
    }

    @Test
    void loadUserByUsername_DeveRetornarUserDetails_QuandoUsuarioAtivoExiste() {
        // Arrange: Configura o usuário como ativo (isDisabled = false)
        user.setDisabled(false);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // Act: Executa o método a ser testado
        UserDetails userDetails = ourUserDetailsService.loadUserByUsername(userEmail);

        // Assert: Verifica se os detalhes do usuário estão corretos e se ele está habilitado
        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getSenha(), userDetails.getPassword());
        assertTrue(userDetails.isEnabled(), "O usuário deveria estar habilitado (enabled = true)");
        assertTrue(userDetails.getAuthorities().isEmpty());
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void loadUserByUsername_DeveRetornarUserDetails_QuandoUsuarioInativoExiste() {
        // Arrange: Configura o usuário como inativo (isDisabled = true)
        user.setDisabled(true);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // Act: Executa o método a ser testado
        UserDetails userDetails = ourUserDetailsService.loadUserByUsername(userEmail);

        // Assert: Verifica se os detalhes do usuário estão corretos e se ele está desabilitado
        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getSenha(), userDetails.getPassword());
        assertFalse(userDetails.isEnabled(), "O usuário deveria estar desabilitado (enabled = false)");
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void loadUserByUsername_DeveLancarUsernameNotFoundException_QuandoUsuarioNaoExiste() {
        // Arrange: Configura o repositório para não encontrar o usuário
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert: Verifica se a exceção correta é lançada
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            ourUserDetailsService.loadUserByUsername(nonExistentEmail);
        });

        assertEquals("Usuário não encontrado com email: " + nonExistentEmail, exception.getMessage());
        verify(userRepository).findByEmail(nonExistentEmail);
    }
}
