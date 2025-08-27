package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.LoginDTO;
import com.cartaGo.cartaGo_backend.dto.LoginResponseDTO;
import com.cartaGo.cartaGo_backend.dto.RegistroUsuarioDto;
import com.cartaGo.cartaGo_backend.dto.UsuarioResponseDTO;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.security.JwtService;
import com.cartaGo.cartaGo_backend.service.ClienteService;
import com.cartaGo.cartaGo_backend.service.RestauranteService;
import com.cartaGo.cartaGo_backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final RestauranteService restauranteService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@RequestBody @Valid RegistroUsuarioDto dto){
        Usuario u = usuarioService.registrar(dto);
        UsuarioResponseDTO res = new UsuarioResponseDTO(
                u.getId(),
                u.getEmail(),
                u.getRol().name()
        );
        if (u.getRol().equals(Usuario.Rol.CLIENTE)){
            clienteService.crearCliente(u, dto.nombre);
        }else{
            restauranteService.crearRestaurante(u,dto.nombre);
        }
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


    @GetMapping("/me")
    public ResponseEntity<MeDto> me(Authentication auth) {
        String email = auth.getName();
        var u = usuarioService.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "No encontrado"));
        return ResponseEntity.ok(new MeDto(u.getId(), u.getEmail(), u.getRol().name()));
    }
    record MeDto(Integer id, String email, String role) {}

}
