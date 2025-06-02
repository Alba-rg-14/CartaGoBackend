package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@IdClass(PlatoAlergenosId.class)
public class PlatoAlergenos {

    @Id
    @ManyToOne
    @JoinColumn(name = "plato_id")
    private Plato plato;

    @Id
    @ManyToOne
    @JoinColumn(name = "alergeno_id")
    private Alergeno alergeno;
}
