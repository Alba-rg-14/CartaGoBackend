package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatoRepository extends JpaRepository<Plato, Integer> {
    List<Plato> findByCartaId(Integer cartaId);
    List<Plato> findBySeccionIgnoreCase(String seccion);
    List<Plato> findByNombreContainingIgnoreCase(String nombre);
}
