package com.cartaGo.cartaGo_backend.entity;

import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Carta")
public class Carta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @OneToMany(mappedBy = "carta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plato> platos = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false, unique = true)
    private Restaurante restaurante;

    // ===== Conveniencia 1:1 =====
    public void setRestaurante(Restaurante r) {
        this.restaurante = r;
        if (r != null && r.getCarta() != this) {
            r.setCarta(this);
        }
    }

    // (Opcional) helpers para 1:N con Plato
    public void addPlato(Plato p) {
        platos.add(p);
        p.setCarta(this);
    }

    public void removePlato(Plato p) {
        platos.remove(p);
        p.setCarta(null);
    }
}
