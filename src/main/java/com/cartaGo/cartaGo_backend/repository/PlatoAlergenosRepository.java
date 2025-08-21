package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.PlatoAlergenos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatoAlergenosRepository extends JpaRepository<PlatoAlergenos, Integer> {
    List<PlatoAlergenos> findByPlatoId(Integer platoId);
    List<PlatoAlergenos> findByAlergenoId(Integer alergenoId);
}
