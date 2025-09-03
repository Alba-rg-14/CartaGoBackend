package com.cartaGo.cartaGo_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.time.DayOfWeek;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "Horario")
@Builder
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia", nullable = false, length = 16)
    private DayOfWeek dia;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime apertura;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime cierre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;


}