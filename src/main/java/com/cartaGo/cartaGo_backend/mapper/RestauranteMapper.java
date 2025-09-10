package com.cartaGo.cartaGo_backend.mapper;

import com.cartaGo.cartaGo_backend.dto.RestauranteDTO.RestauranteDTO;
import com.cartaGo.cartaGo_backend.entity.Restaurante;

import static com.cartaGo.cartaGo_backend.entity.Restaurante.Estado.cerrado;

public final class RestauranteMapper {
    private RestauranteMapper() {}

    public static RestauranteDTO toDTO(Restaurante r) {
        if (r == null) return null;
        return RestauranteDTO.builder()
                .id(r.getId())
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .direccion(r.getDireccion())
                .lat(r.getLat())
                .lon(r.getLon())
                .imagen(r.getImagen())
                .estado(r.getEstado() != null ? r.getEstado() : cerrado)
                .build();
    }
}
