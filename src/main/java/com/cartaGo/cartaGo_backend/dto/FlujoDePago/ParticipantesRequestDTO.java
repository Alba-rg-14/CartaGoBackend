package com.cartaGo.cartaGo_backend.dto.FlujoDePago;

import lombok.Data;

import java.util.List;

@Data
public class ParticipantesRequestDTO {
    private List<Integer> clienteIds;
}
