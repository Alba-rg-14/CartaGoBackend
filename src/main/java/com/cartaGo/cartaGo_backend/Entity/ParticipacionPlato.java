package com.cartaGo.cartaGo_backend.Entity;

import com.cartaGo.cartaGo_backend.Entity.id.ParticipacionPlatoId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipacionPlato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "plato_sala_id")
    private PlatoSala platoSala;

}