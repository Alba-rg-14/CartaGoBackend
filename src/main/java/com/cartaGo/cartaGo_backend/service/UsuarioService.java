package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.RegistroUsuarioDto;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
}
