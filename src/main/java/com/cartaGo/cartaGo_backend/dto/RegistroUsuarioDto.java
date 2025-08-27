package com.cartaGo.cartaGo_backend.dto;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import jakarta.validation.constraints.NotNull;

public class RegistroUsuarioDto {
    @NotNull public String email;
    @NotNull public Usuario.Rol rol;
    @NotNull public String password; // SOLO para entrada, NO se guarda en la entidad
    @NotNull public String nombre;
}
