package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.PlatoAlergenos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlatoAlergenosRepository extends JpaRepository<PlatoAlergenos, Integer> {
    List<PlatoAlergenos> findByPlatoId(Integer platoId);
    List<PlatoAlergenos> findByAlergenoId(Integer alergenoId);

    @Modifying
    @Query("delete from PlatoAlergenos pa where pa.plato.id = :platoId")
    int deleteAllByPlatoId(@Param("platoId") Integer platoId);
}
