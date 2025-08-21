package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.Alergeno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlergenoRepository extends JpaRepository<Alergeno, Integer> {
    Optional<Alergeno> findByNombreIgnoreCase(String nombre);
}
