package com.cartaGo.cartaGo_backend.Entity.id;

import lombok.Data;

import java.io.Serializable;

@Data
public class ParticipacionPlatoId implements Serializable {
    private Integer cliente;
    private Integer platoSala;
}