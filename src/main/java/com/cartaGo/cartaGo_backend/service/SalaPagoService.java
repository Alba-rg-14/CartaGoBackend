package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.FlujoDePago.*;
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
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public void deletePlatoDeSala(Integer salaId, Integer platoSalaId) {
        // 1) sala abierta
        SalaPago sala = salaPagoRepository.findById(salaId)
                .orElseThrow(() -> new EntityNotFoundException("Sala no encontrada: " + salaId));
        if (sala.getEstado() != SalaPago.EstadoSala.abierta) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sala está cerrada");
        }

        // 2) platoSala existe y pertenece a la sala
        PlatoSala ps = platoSalaRepository.findById(platoSalaId)
                .orElseThrow(() -> new EntityNotFoundException("PlatoSala no encontrado: " + platoSalaId));
        if (!Objects.equals(ps.getSalaPago().getId(), salaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plato no pertenece a esta sala");
        }

        // 3) borrar dependencias (participaciones del plato)
        participacionPlatoRepository.deleteByPlatoSalaId(platoSalaId);

        // 4) borrar el PlatoSala
        platoSalaRepository.deleteById(platoSalaId);
    }


    @Transactional
    public void replaceParticipantesDeUnPlato(Integer salaId, Integer platoSalaId, List<Integer> desiredClienteIds) {
        // Normaliza entrada (permite lista vacía => dejar a 0 participantes)
        if (desiredClienteIds == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clienteIds es obligatorio (puede ser lista vacía)");
        }

        // Quita nulls y duplicados
        Set<Integer> desired = desiredClienteIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Validaciones: sala abierta
        SalaPago sala = salaPagoRepository.findById(salaId)
                .orElseThrow(() -> new EntityNotFoundException("Sala no encontrada: " + salaId));
        if (sala.getEstado() != SalaPago.EstadoSala.abierta) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sala está cerrada");
        }

        // Validación: platoSala existe y pertenece a la sala
        PlatoSala ps = platoSalaRepository.findById(platoSalaId)
                .orElseThrow(() -> new EntityNotFoundException("PlatoSala no encontrado: " + platoSalaId));
        if (!Objects.equals(ps.getSalaPago().getId(), salaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plato no pertenece a esta sala");
        }

        // Validación: todos los desired deben estar en la sala (ParticipacionSala)
        for (Integer clienteId : desired) {
            if (!participacionSalaRepository.existsBySalaPagoIdAndClienteId(sala.getId(), clienteId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El cliente " + clienteId + " no está en la sala");
            }
        }

        // Estado actual
        Set<Integer> current = new HashSet<>(participacionPlatoRepository.findClienteIdsByPlatoSalaId(platoSalaId));

        // Diferencias
        Set<Integer> toAdd = new HashSet<>(desired);
        toAdd.removeAll(current);

        Set<Integer> toRemove = new HashSet<>(current);
        toRemove.removeAll(desired);

        // Añadir los que faltan
        if (!toAdd.isEmpty()) {
            List<ParticipacionPlato> nuevos = new ArrayList<>(toAdd.size());
            for (Integer clienteId : toAdd) {
                Cliente cliente = clienteRepository.findById(clienteId)
                        .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clienteId));
                ParticipacionPlato pp = ParticipacionPlato.builder()
                        .cliente(cliente)
                        .platoSala(ps)
                        .build();
                nuevos.add(pp);
            }
            participacionPlatoRepository.saveAll(nuevos);
        }

        // Eliminar los que sobran
        if (!toRemove.isEmpty()) {
            participacionPlatoRepository.deleteByPlatoSalaIdAndClienteIdIn(platoSalaId, toRemove);
        }
    }


    @Transactional
    public void addParticipantesDeUnPlato(Integer salaId, Integer platoSalaId, List<Integer> clienteIds) {
        if (clienteIds == null || clienteIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clienteIds es obligatorio");
        }

        // sala abierta
        SalaPago sala = salaPagoRepository.findById(salaId)
                .orElseThrow(() -> new EntityNotFoundException("Sala no encontrada: " + salaId));
        if (sala.getEstado() != SalaPago.EstadoSala.abierta) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sala está cerrada");
        }

        // platoSala pertenece a la sala
        PlatoSala ps = platoSalaRepository.findById(platoSalaId)
                .orElseThrow(() -> new EntityNotFoundException("PlatoSala no encontrado: " + platoSalaId));
        if (!Objects.equals(ps.getSalaPago().getId(), salaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plato no pertenece a esta sala");
        }

        // validar: cada cliente debe estar en ParticipacionSala de la sala
        for (Integer clienteId : clienteIds) {
            if (!participacionSalaRepository.existsBySalaPagoIdAndClienteId(sala.getId(), clienteId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El cliente " + clienteId + " no está en la sala");
            }
        }

        // crear participaciones si no existen
        for (Integer clienteId : clienteIds) {
            if (!participacionPlatoRepository.existsByPlatoSalaIdAndClienteId(platoSalaId, clienteId)) {
                Cliente cliente = clienteRepository.findById(clienteId)
                        .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clienteId));

                ParticipacionPlato pp = ParticipacionPlato.builder()
                        .cliente(cliente)
                        .platoSala(ps)
                        .build();
                participacionPlatoRepository.save(pp);
            }
        }
    }

    @Transactional
    public void removeParticipantesDeUnPlato(Integer salaId, Integer platoSalaId, List<Integer> clienteIds) {
        if (clienteIds == null || clienteIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clienteIds es obligatorio");
        }

        SalaPago sala = salaPagoRepository.findById(salaId)
                .orElseThrow(() -> new EntityNotFoundException("Sala no encontrada: " + salaId));
        if (sala.getEstado() != SalaPago.EstadoSala.abierta) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sala está cerrada");
        }

        PlatoSala ps = platoSalaRepository.findById(platoSalaId)
                .orElseThrow(() -> new EntityNotFoundException("PlatoSala no encontrado: " + platoSalaId));
        if (!Objects.equals(ps.getSalaPago().getId(), salaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plato no pertenece a esta sala");
        }

        // borrar participaciones (si existen)
        for (Integer clienteId : clienteIds) {
            participacionPlatoRepository.deleteByPlatoSalaIdAndClienteId(platoSalaId, clienteId);
        }
    }

    @Transactional(readOnly = true)
    public SalaResumenDTO getResumenSala(Integer salaId) {
        var sala = salaPagoRepository.findById(salaId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Sala no encontrada: " + salaId));

        // Comensales de la sala
        var comensales = participacionSalaRepository.findBySalaPagoId(salaId).stream()
                .map(ps -> new ComensalDTO(ps.getCliente().getId(), ps.getCliente().getNombre())) // ajusta campo nombre
                .toList();

        // Platos de la sala
        var platosSala = platoSalaRepository.findBySalaPagoId(salaId);

        // Mapear cada PlatoSala con sus participantes y precio actual (del Plato)
        var platos = platosSala.stream().map(ps -> {
            var participantes = participacionPlatoRepository.findByPlatoSalaId(ps.getId()).stream()
                    .map(pp -> new ComensalDTO(pp.getCliente().getId(), pp.getCliente().getNombre()))
                    .toList();

            var precio = ps.getPlato().getPrecio(); // precio actual desde la carta
            return PlatoSalaResumenDTO.builder()
                    .platoSalaId(ps.getId())
                    .platoId(ps.getPlato().getId())
                    .platoNombre(ps.getPlato().getNombre())
                    .precioActual(precio)
                    .participantes(participantes)
                    .build();
        }).toList();

        // Subtotal (suma de precios actuales)
        java.math.BigDecimal subtotal = platos.stream()
                .map(PlatoSalaResumenDTO::getPrecioActual)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return SalaResumenDTO.builder()
                .salaId(sala.getId())
                .codigo(sala.getCodigo())
                .estado(sala.getEstado().name())
                .restauranteId(sala.getRestaurante().getId())
                .fechaCreacion(sala.getFechaCreacion())
                .comensales(comensales)
                .platos(platos)
                .subtotal(subtotal)
                .build();
    }

    @Transactional
    public InstruccionesPagoDTO generarInstruccionesDetalladas(Integer salaId, String modoRaw) {
        var sala = salaPagoRepository.findById(salaId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Sala no encontrada: " + salaId));
        if (sala.getEstado() == SalaPago.EstadoSala.cerrada) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "La sala ya está cerrada");
        }

        final String modo = (modoRaw == null ? "" : modoRaw.trim().toLowerCase());
        if (!modo.equals("igualitario") && !modo.equals("personalizado")) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "modo debe ser 'igualitario' o 'personalizado'");
        }

        // Participantes
        var partSala = participacionSalaRepository.findBySalaPagoId(salaId);
        if (partSala.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "La sala no tiene comensales");
        }
        var clientes = partSala.stream().map(ParticipacionSala::getCliente).toList();
        var clienteMap = clientes.stream().collect(java.util.stream.Collectors.toMap(Cliente::getId, c -> c));

        // Platos
        var platosSala = platoSalaRepository.findBySalaPagoId(salaId);
        if (platosSala.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "La sala no tiene platos");
        }

        // Subtotal con precio ACTUAL (no congelado)
        java.math.BigDecimal subtotal = platosSala.stream()
                .map(ps -> ps.getPlato().getPrecio())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Acumuladores por cliente
        java.util.Map<Integer, java.util.List<DetallePlatoClienteDTO>> detallesPorCliente = new java.util.HashMap<>();
        java.util.Map<Integer, java.math.BigDecimal> totalPorCliente = new java.util.HashMap<>();
        for (var c : clientes) {
            detallesPorCliente.put(c.getId(), new java.util.ArrayList<>());
            totalPorCliente.put(c.getId(), java.math.BigDecimal.ZERO);
        }

        final var HM = java.math.RoundingMode.HALF_UP;

        if (modo.equals("personalizado")) {
            // Reparto por plato entre sus participantes
            for (var ps : platosSala) {
                var precioPlato = ps.getPlato().getPrecio().setScale(2, HM);

                var pps = participacionPlatoRepository.findByPlatoSalaId(ps.getId());
                java.util.List<Cliente> consumers = pps.isEmpty()
                        ? clientes
                        : pps.stream().map(ParticipacionPlato::getCliente).toList();

                int m = consumers.size();
                var base = precioPlato.divide(new java.math.BigDecimal(m), 2, HM);

                java.util.Map<Integer, java.math.BigDecimal> partes = new java.util.LinkedHashMap<>();
                for (var c : consumers) partes.put(c.getId(), base);
                ajustarCentimos(partes, precioPlato); // suma(partes)=precioPlato

                for (var e : partes.entrySet()) {
                    var cid = e.getKey();
                    var share = e.getValue().setScale(2, HM);

                    detallesPorCliente.get(cid).add(
                            DetallePlatoClienteDTO.builder()
                                    .platoSalaId(ps.getId())
                                    .platoId(ps.getPlato().getId())
                                    .platoNombre(ps.getPlato().getNombre())
                                    .precioPlato(precioPlato)
                                    .tuParte(share)
                                    .build()
                    );
                    totalPorCliente.put(cid, totalPorCliente.get(cid).add(share));
                }
            }
            // Ajuste final por seguridad (suma totales = subtotal)
            ajustarCentimos(totalPorCliente, subtotal.setScale(2, HM));

        } else { // igualitario
            int n = clientes.size();
            var objetivoPorCliente = subtotal.divide(new java.math.BigDecimal(n), 2, HM);

            // Para dar detalle: cada plato se reparte entre TODOS (precioPlato/n)
            for (var ps : platosSala) {
                var precioPlato = ps.getPlato().getPrecio().setScale(2, HM);
                var base = precioPlato.divide(new java.math.BigDecimal(n), 2, HM);

                java.util.Map<Integer, java.math.BigDecimal> partes = new java.util.LinkedHashMap<>();
                for (var c : clientes) partes.put(c.getId(), base);
                ajustarCentimos(partes, precioPlato);

                for (var e : partes.entrySet()) {
                    var cid = e.getKey();
                    var share = e.getValue().setScale(2, HM);

                    detallesPorCliente.get(cid).add(
                            DetallePlatoClienteDTO.builder()
                                    .platoSalaId(ps.getId())
                                    .platoId(ps.getPlato().getId())
                                    .platoNombre(ps.getPlato().getNombre())
                                    .precioPlato(precioPlato)
                                    .tuParte(share)
                                    .build()
                    );
                    totalPorCliente.put(cid, totalPorCliente.get(cid).add(share));
                }
            }
            // Asegura que la suma por cliente total = subtotal/n y global = subtotal
            ajustarCentimos(totalPorCliente, subtotal.setScale(2, HM));
        }

        // Construir porCliente (usando email desde cliente.usuario.email)
        java.util.List<LineaInstruccionDTO> porCliente = clientes.stream().map(c -> {
            String email = null;
            try {
                var u = c.getUsuario();
                if (u != null && u.getEmail() != null) email = u.getEmail();
            } catch (Exception ignore) {}
            return new LineaInstruccionDTO(
                    c.getId(),
                    c.getNombre(),      // cambia si tu campo se llama distinto
                    email,
                    totalPorCliente.get(c.getId()).setScale(2, HM),
                    detallesPorCliente.get(c.getId())
            );
        }).toList();

        // Cerrar sala y fijar modo
        sala.setFormaDePago(modo.equals("igualitario")
                ? SalaPago.FormaDePago.pago_igualitario
                : SalaPago.FormaDePago.pago_personalizado);
        sala.setEstado(SalaPago.EstadoSala.cerrada);
        salaPagoRepository.save(sala);

        return InstruccionesPagoDTO.builder()
                .salaId(sala.getId())
                .restauranteNombre(sala.getRestaurante().getNombre())
                .fechaGeneracion(java.time.LocalDateTime.now())
                .modo(modo) // "igualitario" | "personalizado"
                .subtotal(subtotal.setScale(2, HM))
                .porCliente(porCliente)
                .build();
    }

    /** Reparte +/- 0.01 para que la suma de valores coincida EXACTAMENTE con target */
    private void ajustarCentimos(java.util.Map<Integer, java.math.BigDecimal> valores, java.math.BigDecimal target) {
        var HM = java.math.RoundingMode.HALF_UP;
        java.math.BigDecimal suma = valores.values().stream()
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .setScale(2, HM);
        java.math.BigDecimal objetivo = target.setScale(2, HM);
        long diff = objetivo.subtract(suma).movePointRight(2).longValue(); // céntimos

        if (diff == 0) return;
        var keys = new java.util.ArrayList<>(valores.keySet());
        int i = 0;
        while (diff != 0) {
            Integer k = keys.get(i % keys.size());
            var v = valores.get(k);
            if (diff > 0) { v = v.add(new java.math.BigDecimal("0.01")); diff--; }
            else {
                if (v.compareTo(new java.math.BigDecimal("0.01")) >= 0) { v = v.subtract(new java.math.BigDecimal("0.01")); diff++; }
                else { i++; continue; }
            }
            valores.put(k, v);
            i++;
        }
    }


}
