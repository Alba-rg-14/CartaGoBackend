package com.cartaGo.cartaGo_backend.dto;
import com.cartaGo.cartaGo_backend.entity.Usuario;

public class RegistroUsuarioDto {
    public String email;
    public Usuario.Rol rol;
    public String password; // SOLO para entrada, NO se guarda en la entidad
}
