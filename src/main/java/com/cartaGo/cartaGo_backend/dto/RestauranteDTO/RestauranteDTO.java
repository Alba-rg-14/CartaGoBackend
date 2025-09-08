package com.cartaGo.cartaGo_backend.dto.RestauranteDTO;

import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioDTO;
import com.cartaGo.cartaGo_backend.entity.Restaurante;
import lombok.Builder;

import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public static RestauranteDTO from(Restaurante r) {
        if (r == null) return null;

        List<HorarioDTO> horariosDto = (r.getHorarios() == null)
                ? List.of()
                : r.getHorarios().stream()
                .map(h -> HorarioDTO.builder()
                        .id(h.getId())
                        .dia(h.getDia())
                        .apertura(h.getApertura() != null ? h.getApertura().format(HM) : null)
                        .cierre(h.getCierre()   != null ? h.getCierre().format(HM)   : null)
                        .build())
                .toList();

        return RestauranteDTO.builder()
                .id(r.getId())
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .imagen(r.getImagen())
                .estado(r.getEstado())
                .direccion(r.getDireccion())
                .lat(r.getLat())
                .lon(r.getLon())
                .horarios(horariosDto)
                .build();
    }

}
