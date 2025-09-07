package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.UsuarioLoginDTO.RegistroUsuarioDto;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        var encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(currentPassword, u.getPassword_hash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña actual incorrecta");
        }

        u.setPassword_hash(encoder.encode(newPassword));
        usuarioRepository.save(u);
    }


    @Transactional
    public void forceSetPasswordByEmail(String email, String newPassword) {
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        var encoder = new BCryptPasswordEncoder();
        u.setPassword_hash(encoder.encode(newPassword));
        usuarioRepository.save(u);
    }


}
