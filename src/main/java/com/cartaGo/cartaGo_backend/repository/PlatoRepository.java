package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlatoRepository extends JpaRepository<Plato, Integer> {
    @Query("""
        SELECT COALESCE(MAX(p.orden), 0)
        FROM Plato p
        WHERE p.carta.id = :cartaId AND p.seccion = :seccion
    """)
    Integer findMaxOrdenByCartaAndSeccion(@Param("cartaId") Integer cartaId,
                                          @Param("seccion") String seccion);

    List<Plato> findByCarta_IdOrderBySeccionAscOrdenAsc(Integer cartaId);

    List<Plato> findByCarta_IdAndSeccionOrderByOrdenAsc(Integer cartaId, String seccion);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Plato p
        SET p.seccion = :dst
        WHERE p.carta.id = :cartaId AND p.seccion = :src
    """)
    int bulkRenameSection(@Param("cartaId") Integer cartaId,
                          @Param("src") String from,
                          @Param("dst") String to);

    boolean existsByCarta_IdAndSeccion(Integer cartaId, String dst);
}
