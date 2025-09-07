package com.cartaGo.cartaGo_backend.dto.UsuarioLoginDTO;

import com.cartaGo.cartaGo_backend.entity.Cliente;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ClienteDTO {
    private Integer id;
    private String nombre;
    private String imagen;
    private Integer usuarioId;

    public static ClienteDTO from(Cliente c) {
        return ClienteDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .imagen(c.getImagen())
                .usuarioId(c.getUsuario().getId())
                .build();
    }
}
