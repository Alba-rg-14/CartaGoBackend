package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime fechaCreacion;

    private Double saldo;

    @Enumerated(EnumType.STRING)
    private FormaDePago formaDePago;

    @Enumerated(EnumType.STRING)
    private EstadoSala estado;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    @OneToMany(mappedBy = "salaPago", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlatoSala> platosSala;

    @OneToMany(mappedBy = "salaPago", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipacionSala> participaciones;

    public enum FormaDePago {
        pago_igualitario, pago_personalizado
    }

    public enum EstadoSala {
        abierta, cerrada
    }
}
