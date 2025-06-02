package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@IdClass(ParticipacionPlatoId.class)
public class ParticipacionPlato {

    @Id
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Id
    @ManyToOne
    @JoinColumn(name = "plato_sala_id")
    private PlatoSala platoSala;
}