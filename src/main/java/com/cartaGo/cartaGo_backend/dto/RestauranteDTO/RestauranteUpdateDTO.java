package com.cartaGo.cartaGo_backend.dto.RestauranteDTO;

import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestauranteUpdateDTO {
    private String nombre;
    private String descripcion;
    private String direccion;
    private Double lat;
    private Double lon;
    private String imagen;        // secure_url de Cloudinary (opcional)
    private String estado;        // "abierto" | "cerrado"
    public List<HorarioDTO> horarios;

}
