package com.cartaGo.cartaGo_backend.dto.RestauranteDTO;

import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioDTO;
import com.cartaGo.cartaGo_backend.entity.Restaurante;
import lombok.Builder;

import java.util.List;

@Builder
public class RestauranteDTO {
    public Integer id;
    public String nombre;
    public String descripcion;
    public String imagen;
    public Restaurante.Estado estado;
    public String direccion;
    public Double lat;
    public Double lon;
    public List<HorarioDTO> horarios;
}
