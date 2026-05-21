package com.leilao.leilaoapp.service;

import com.leilao.leilaoapp.dto.RegisterDTO;
import com.leilao.leilaoapp.entity.User;
import com.leilao.leilaoapp.entity.enums.UserRole;
import com.leilao.leilaoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    public User registerUser(RegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado: " + dto.getEmail());
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.ROLE_USER)
                .codigoBidder(gerarCodigoUnico())
                .build();

        return userRepository.save(user);
    }

    /**
     * Gera um código inteiro de 6 dígitos único para o licitante.
     */
    private Integer gerarCodigoUnico() {
        int codigo;
        int tentativas = 0;
        do {
            codigo = ThreadLocalRandom.current().nextInt(100000, 1000000);
            tentativas++;
            if (tentativas > 50) break; // segurança contra loop infinito
        } while (userRepository.existsByCodigoBidder(codigo));
        return codigo;
    }
}
