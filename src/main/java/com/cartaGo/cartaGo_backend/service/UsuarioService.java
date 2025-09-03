package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.UsuarioLoginDTO.RegistroUsuarioDto;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // REGISTRO: guarda el hash
    public Usuario registrar(RegistroUsuarioDto dto) {
        //validar que no exista email/username
        usuarioRepository.findByEmailIgnoreCase(dto.email)
                .ifPresent(u -> { throw new RuntimeException("El email ya está en uso"); });

        var encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(dto.password); // <- Hash

        Usuario u = new Usuario();
        u.setEmail(dto.email);
        u.setPassword_hash(hash);
        u.setRol(dto.rol);
        return usuarioRepository.save(u);
    }

    // LOGIN: compara texto vs hash
    public boolean login(String email, String passwordEnClaro) {
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var encoder = new BCryptPasswordEncoder();
        return encoder.matches(passwordEnClaro, u.getPassword_hash());
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email);
    }
    public boolean verificarPassword(Usuario u, String passwordEnClaro) {
        var encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        return encoder.matches(passwordEnClaro, u.getPassword_hash());
    }

}
