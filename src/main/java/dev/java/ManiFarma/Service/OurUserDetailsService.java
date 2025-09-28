package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.Entity.User;
import dev.java.ManiFarma.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class OurUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + username));

        // Aqui você pode adicionar as roles/autoridades do usuário se tiver
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getSenha(), // A senha já está criptografada no banco de dados
                new ArrayList<>() // Lista vazia de autoridades por enquanto
        );
    }
}
