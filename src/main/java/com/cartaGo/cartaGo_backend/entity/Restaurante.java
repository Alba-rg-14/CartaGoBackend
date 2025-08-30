package com.cartaGo.cartaGo_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"carta", "usuario", "horarios"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Restaurante")
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 255)
    private String direccion;

    private Double lat;
    private Double lon;

    @Column(length = 255)
    private String imagen;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    public enum Estado {
        abierto, cerrado
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @OneToOne(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Carta carta;

    @OneToMany(mappedBy = "restaurante",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Horario> horarios = new ArrayList<>();

    public void setCarta(Carta nueva) {
        if (this.carta != null) {
            this.carta.setRestaurante(null);
        }
        this.carta = nueva;
        if (nueva != null && nueva.getRestaurante() != this) {
            nueva.setRestaurante(this);
        }
    }

    public void removeCarta() {
        if (this.carta != null) {
            this.carta.setRestaurante(null);
            this.carta = null; // orphanRemoval borrará la carta al commit
        }
    }

}
