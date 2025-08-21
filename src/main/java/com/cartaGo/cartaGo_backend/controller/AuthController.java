package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.LoginDTO;
import com.cartaGo.cartaGo_backend.dto.LoginResponseDTO;
import com.cartaGo.cartaGo_backend.dto.RegistroUsuarioDto;
import com.cartaGo.cartaGo_backend.dto.UsuarioResponseDTO;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

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
        boolean ok = usuarioService.login(dto.email, dto.password);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponseDTO(false));
        }
        return ResponseEntity.ok(new LoginResponseDTO(true));
    }
}
