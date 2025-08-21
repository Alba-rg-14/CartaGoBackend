package com.cartaGo.cartaGo_backend.entity;

import com.cartaGo.cartaGo_backend.entity.id.PlatoAlergenosId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PlatoAlergenosId.class)
@Table(name = "PlatoAlergenos")
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