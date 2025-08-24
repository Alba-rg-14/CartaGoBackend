package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.LoginDTO;
import com.cartaGo.cartaGo_backend.dto.LoginResponseDTO;
import com.cartaGo.cartaGo_backend.dto.RegistroUsuarioDto;
import com.cartaGo.cartaGo_backend.dto.UsuarioResponseDTO;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.security.JwtService;
import com.cartaGo.cartaGo_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@RequestBody RegistroUsuarioDto dto){
        Usuario u = usuarioService.registrar(dto);
        UsuarioResponseDTO res = new UsuarioResponseDTO(
                u.getId(),
                u.getEmail(),
                u.getRol().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO dto) {
        var u = usuarioService.findByEmail(dto.email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        if (!usuarioService.verificarPassword(u, dto.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = jwtService.generateAccessToken(u);
        var res = new LoginResponseDTO(token, "Bearer", jwtService.getExpiresSeconds(), u.getId(), u.getRol().name());
        return ResponseEntity.ok(res);
    }
}
