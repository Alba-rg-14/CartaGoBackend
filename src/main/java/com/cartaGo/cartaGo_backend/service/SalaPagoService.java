package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.FlujoDePago.AddPlatoRequestDTO;
import com.cartaGo.cartaGo_backend.dto.FlujoDePago.AddPlatoResponseDTO;
import com.cartaGo.cartaGo_backend.dto.FlujoDePago.SalaCreateResponseDTO;
import com.cartaGo.cartaGo_backend.entity.*;
import com.cartaGo.cartaGo_backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SalaPagoService {
    private final SalaPagoRepository salaPagoRepository;
    private final RestauranteRepository restauranteRepository;
    private final ClienteRepository clienteRepository;
    private final ParticipacionSalaRepository participacionSalaRepository;
    private final PlatoSalaRepository platoSalaRepository;
    private final PlatoRepository platoRepository;
    private final ParticipacionPlatoRepository participacionPlatoRepository;

    @Transactional
    public SalaCreateResponseDTO crearSala(Integer restauranteId, Integer clienteCreadorId) {
        if (clienteCreadorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clienteId es obligatorio");
        }

        var restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado: " + restauranteId));

        var cliente = clienteRepository.findById(clienteCreadorId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clienteCreadorId));

        String codigo = generarCodigoUnico();

        var sala = SalaPago.builder()
                .restaurante(restaurante)
                .fechaCreacion(LocalDateTime.now())
                .estado(SalaPago.EstadoSala.abierta)
                .formaDePago(null)
                .saldo(0d)
                .codigo(codigo)
                .build();
        sala = salaPagoRepository.save(sala);

        if (!participacionSalaRepository.existsBySalaPagoIdAndClienteId(sala.getId(), cliente.getId())) {
            ParticipacionSala ps = ParticipacionSala.builder()
                    .cliente(cliente)
                    .salaPago(sala)
                    .monto(java.math.BigDecimal.ZERO)
                    .build();
            participacionSalaRepository.saveAndFlush(ps);
        }

        return SalaCreateResponseDTO.builder()
                .salaId(sala.getId())
                .codigo(sala.getCodigo())
                .estado(sala.getEstado().name())
                .fechaCreacion(sala.getFechaCreacion().toString())
                .build();
    }


    private String generarCodigoUnico() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sin confusos (I,1,O,0)
        java.util.Random r = new java.util.Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
            code = sb.toString();
        } while (salaPagoRepository.findByCodigo(code).isPresent());
        return code;
    }

    @Transactional
    public SalaCreateResponseDTO unirseASala(Integer restauranteId, String codigoRaw, Integer clienteId) {
        if (clienteId == null || codigoRaw == null || codigoRaw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clienteId y codigo son obligatorios");
        }
        String codigo = codigoRaw.trim().toUpperCase();

        var sala = salaPagoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Sala no encontrada para el código: " + codigo));

        if (!Objects.equals(sala.getRestaurante().getId(), restauranteId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código no pertenece a este restaurante");
        }
        if (sala.getEstado() != SalaPago.EstadoSala.abierta) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sala está cerrada");
        }

        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clienteId));

        // evita duplicados
        if (!participacionSalaRepository.existsBySalaPagoIdAndClienteId(sala.getId(), cliente.getId())) {
            participacionSalaRepository.save(
                    ParticipacionSala.builder()
                            .cliente(cliente)
                            .salaPago(sala)
                            .monto(java.math.BigDecimal.ZERO) // se calculará al final
                            .build()
            );
        }

        return SalaCreateResponseDTO.builder()
                .salaId(sala.getId())
                .codigo(sala.getCodigo())
                .estado(sala.getEstado().name())
                .fechaCreacion(sala.getFechaCreacion().toString())
                .build();
    }

    // service/SalaPagoService.java (añade esto)
    @Transactional
    public AddPlatoResponseDTO addPlato(Integer salaId, AddPlatoRequestDTO req) {
        if (req == null || req.getPlatoId() == null || req.getParticipantes() == null || req.getParticipantes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "platoId y participantes son obligatorios");
        }

        // 1) sala abierta
        var sala = salaPagoRepository.findById(salaId)
                .orElseThrow(() -> new EntityNotFoundException("Sala no encontrada: " + salaId));
        if (sala.getEstado() != SalaPago.EstadoSala.abierta) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sala está cerrada");
        }

        // 2) plato del mismo restaurante
        var plato = platoRepository.findById(req.getPlatoId())
                .orElseThrow(() -> new EntityNotFoundException("Plato no encontrado: " + req.getPlatoId()));
        if (plato.getCarta() == null || plato.getCarta().getRestaurante() == null
                || !Objects.equals(plato.getCarta().getRestaurante().getId(), sala.getRestaurante().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plato no pertenece a este restaurante");
        }

        // 3) validar que todos los participantes pertenecen a la sala
        for (Integer clienteId : req.getParticipantes()) {
            if (!participacionSalaRepository.existsBySalaPagoIdAndClienteId(sala.getId(), clienteId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El cliente " + clienteId + " no está en la sala");
            }
        }

        // 4) crear PlatoSala (snapshot del precio)
        var platoSala = PlatoSala.builder()
                .salaPago(sala)
                .plato(plato)
                .build();
        platoSala = platoSalaRepository.save(platoSala);

        // 5) crear ParticipacionPlato (una por cliente; reparto a partes iguales se hará al calcular)
        for (Integer clienteId : req.getParticipantes()) {
            var cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clienteId));

            var pp = ParticipacionPlato.builder()
                    .cliente(cliente)
                    .platoSala(platoSala)
                    .build();
            participacionPlatoRepository.save(pp);
        }

        return AddPlatoResponseDTO.builder()
                .platoSalaId(platoSala.getId())
                .platoId(plato.getId())
                .platoNombre(plato.getNombre())
                .precioUnitario(plato.getPrecio())
                .participantes(req.getParticipantes().size())
                .build();
    }


}
