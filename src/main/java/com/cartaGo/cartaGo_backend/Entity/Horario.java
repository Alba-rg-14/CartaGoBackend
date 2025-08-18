package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Horario")
@Builder
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private DiaSemana dia;

    private LocalTime hora_inicio;
    private LocalTime hora_fin;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    public enum DiaSemana {
        lunes, martes, miércoles, jueves, viernes, sábado, domingo
    }
}