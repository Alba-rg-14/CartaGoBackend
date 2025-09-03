package com.cartaGo.cartaGo_backend.dto.FlujoDePago;

import lombok.Data;

import java.util.List;

@Data
public class AddPlatoRequestDTO {
    private Integer platoId;
    private List<Integer> participantes;
}
