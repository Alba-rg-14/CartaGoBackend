package com.cartaGo.cartaGo_backend.entity;

import com.cartaGo.cartaGo_backend.entity.id.PlatoAlergenosId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data  @Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PlatoAlergenosId.class)
@Table(name = "PlatoAlergenos")
public class PlatoAlergenos {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plato_id", nullable = false)
    private Plato plato;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alergeno_id", nullable = false)
    private Alergeno alergeno;
}