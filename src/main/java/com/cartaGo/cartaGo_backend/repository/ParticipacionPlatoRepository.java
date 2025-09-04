package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.Cliente;
import com.cartaGo.cartaGo_backend.entity.ParticipacionPlato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipacionPlatoRepository extends JpaRepository<ParticipacionPlato, Cliente> {

    // por platoSala (qué plato se compartió en la sala) o por cliente (usando id)
    List<ParticipacionPlato> findByPlatoSalaId(Integer platoSalaId);
    List<ParticipacionPlato> findByClienteId(Integer clienteId);

    // participación concreta (cliente x platoSala)
    Optional<ParticipacionPlato> findByPlatoSalaIdAndClienteId(Integer platoSalaId, Integer clienteId);

    // grupos de clientes (para ver reparto de un plato entre varias personas)
    List<ParticipacionPlato> findByClienteIdIn(Collection<Integer> clienteIds);
    List<ParticipacionPlato> findByPlatoSalaIdAndClienteIdIn(Integer platoSalaId, Collection<Integer> clienteIds);

    boolean existsByPlatoSalaIdAndClienteId(Integer platoSalaId, Integer clienteId);

    void deleteByPlatoSalaIdAndClienteId(Integer platoSalaId, Integer clienteId);

    @Query("select pp.cliente.id from ParticipacionPlato pp where pp.platoSala.id = :platoSalaId")
    List<Integer> findClienteIdsByPlatoSalaId(@Param("platoSalaId") Integer platoSalaId);

    @Modifying
    @Query("delete from ParticipacionPlato pp where pp.platoSala.id = :platoSalaId and pp.cliente.id in :clienteIds")
    void deleteByPlatoSalaIdAndClienteIdIn(@Param("platoSalaId") Integer platoSalaId,
                                           @Param("clienteIds") Collection<Integer> clienteIds);

    @Modifying
    @Query("delete from ParticipacionPlato pp where pp.platoSala.id = :platoSalaId")
    void deleteByPlatoSalaId(@Param("platoSalaId") Integer platoSalaId);
}
