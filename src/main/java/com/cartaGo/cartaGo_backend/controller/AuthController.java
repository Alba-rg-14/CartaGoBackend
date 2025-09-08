package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.RestauranteDTO.RestauranteDTO;
import com.cartaGo.cartaGo_backend.dto.UsuarioLoginDTO.*;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.security.JwtService;
import com.cartaGo.cartaGo_backend.service.ClienteService;
import com.cartaGo.cartaGo_backend.service.MailService;
import com.cartaGo.cartaGo_backend.service.RestauranteService;
import com.cartaGo.cartaGo_backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
@CrossOrigin(
        origins = {
                "http://localhost:8081",
                "http://127.0.0.1:8081",
                "https://*.expo.dev"
        }
)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final RestauranteService restauranteService;
    private final MailService mailService;

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

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(Authentication auth,
                               @RequestBody @Valid ChangePasswordRequestDTO req) {
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        usuarioService.changePassword(auth.getName(), req.getCurrentPassword(), req.getNewPassword());
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

    // DTOs simples
    public record ResetPasswordRequest(String email) {}
    public record ResetPasswordConfirmRequest(String token, String newPassword) {}

    @PostMapping("/reset-password/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestReset(@RequestBody ResetPasswordRequest req) {
        // 1) buscar usuario (si no existe, devolver 204 igualmente para no filtrar emails)
        usuarioService.findByEmail(req.email()); // no hace falta usar el return

        // 2) generar token tiempo limitado (p.ej. 15 min)
        String token = jwtService.generateResetPasswordToken(req.email(), 15);

        // 3) enviar email con enlace
        String resetLink = "https://cartago-44hbc017m-albas-projects-ec9d8895.vercel.app/auth/reset-confirm?token=" + token;
        mailService.send(
                req.email(),
                "Recupera tu contraseña",
                """
                Hola,
                
                Has solicitado restablecer tu contraseña. Pulsa este enlace:
                %s
                
                Si no fuiste tú, ignora este correo.
                """.formatted(resetLink)
        );
    }

    @PostMapping("/reset-password/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmReset(@RequestBody ResetPasswordConfirmRequest req) {
        if (req.newPassword() == null || req.newPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña mínima 8 caracteres");
        }
        String email = jwtService.validateAndGetEmailFromResetToken(req.token());
        usuarioService.forceSetPasswordByEmail(email, req.newPassword());
    }

    @GetMapping("/{usuarioId}/cliente")
    public ClienteDTO getClienteByUsuario(@PathVariable Integer usuarioId) {
        return clienteService.getByUsuarioId(usuarioId);
    }


    @GetMapping("/{usuarioId}/restaurante")
    public RestauranteDTO getRestauranteByUsuario(@PathVariable Integer usuarioId) {
        return restauranteService.getByUsuarioId(usuarioId);
    }
}
