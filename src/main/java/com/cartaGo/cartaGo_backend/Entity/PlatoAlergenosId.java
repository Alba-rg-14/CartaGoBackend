package com.cartaGo.cartaGo_backend.Entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlatoAlergenosId implements Serializable {
    private Integer plato;
    private Integer alergeno;
}