package com.cartaGo.cartaGo_backend.Entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ParticipacionSalaId implements Serializable {
    private Integer cliente;
    private Integer salaPago;
}