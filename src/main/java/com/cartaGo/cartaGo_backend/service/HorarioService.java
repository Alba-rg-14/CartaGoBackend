package com.cartaGo.cartaGo_backend.service;


import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioDTO;
import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioRequestDTO;
import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioRequestSemanalDTO;
import com.cartaGo.cartaGo_backend.entity.Horario;
import com.cartaGo.cartaGo_backend.entity.Restaurante;
import com.cartaGo.cartaGo_backend.repository.HorarioRepository;
import com.cartaGo.cartaGo_backend.repository.RestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final RestauranteRepository restauranteRepository;

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");


    public List<HorarioDTO> listarHorarios(Integer restauranteId) {
        return horarioRepository
                .findByRestauranteIdOrderByDiaAscAperturaAsc(restauranteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Reemplaza TODOS los tramos de la semana del restaurante por los recibidos.
     * Si un día no aparece en la lista => ese día queda CERRADO (sin tramos).
     */
    @Transactional
    public List<HorarioDTO> putSemanal(Integer restauranteId, HorarioRequestSemanalDTO req) {
        Restaurante r = findRestaurante(restauranteId);
        List<HorarioRequestDTO> tramos = Optional.ofNullable(req.getTramos()).orElse(List.of());

        Map<DayOfWeek, List<Horario>> nuevosPorDia = agruparValidandoPorDia(r, tramos);

        horarioRepository.deleteByRestauranteId(restauranteId);
        List<Horario> guardados = new ArrayList<>();
        for (var entry : nuevosPorDia.entrySet()) {
            guardados.addAll(horarioRepository.saveAll(entry.getValue()));
        }
        guardados.sort(Comparator
                .comparing(Horario::getDia)
                .thenComparing(Horario::getApertura));

        return guardados.stream().map(this::toDTO).collect(Collectors.toList());
    }


    @Transactional
    public HorarioDTO crearTramo(Integer restauranteId, HorarioRequestDTO slot) {
        Restaurante r = findRestaurante(restauranteId);

        DayOfWeek dia = require(slot.getDia(), "El campo 'dia' es obligatorio");
        LocalTime apertura = parseHora(require(slot.getApertura(), "El campo 'apertura' es obligatorio"));
        LocalTime cierre   = parseHora(require(slot.getCierre(),   "El campo 'cierre' es obligatorio"));
        validarTramoBasico(apertura, cierre);

        List<Horario> existentes = horarioRepository.findByRestauranteIdAndDiaOrderByAperturaAsc(restauranteId, dia);
        validarSinSolapes(apertura, cierre, existentes, null);

        Horario h = Horario.builder()
                .restaurante(r)
                .dia(dia)
                .apertura(apertura)
                .cierre(cierre)
                .build();
        h = horarioRepository.save(h);
        return toDTO(h);
    }

    @Transactional
    public List<HorarioDTO> appendSemanal(Integer restauranteId, HorarioRequestSemanalDTO req) {
        Restaurante r = findRestaurante(restauranteId);
        List<HorarioRequestDTO> tramos = Optional.ofNullable(req.getTramos()).orElse(List.of());

        Map<DayOfWeek, List<Horario>> nuevosPorDia = agruparValidandoPorDia(r, tramos);

        List<Horario> aGuardar = new ArrayList<>();
        for (var entry : nuevosPorDia.entrySet()) {
            DayOfWeek dia = entry.getKey();
            List<Horario> nuevos = entry.getValue();                // ya ordenados y sin solapes entre ellos
            List<Horario> existentes = horarioRepository
                    .findByRestauranteIdAndDiaOrderByAperturaAsc(restauranteId, dia);

            for (Horario n : nuevos) {
                validarSinSolapes(n.getApertura(), n.getCierre(), existentes, null);
                aGuardar.add(n);
            }
        }

        aGuardar = horarioRepository.saveAll(aGuardar);
        aGuardar.sort(Comparator.comparing(Horario::getDia).thenComparing(Horario::getApertura));
        return aGuardar.stream().map(this::toDTO).toList();
    }



    @Transactional
    public void borrarTramo(Integer horarioId) {
        horarioRepository.deleteById(horarioId);
    }


    private Restaurante findRestaurante(Integer restauranteId) {
        return restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado: " + restauranteId));
    }

    private Map<DayOfWeek, List<Horario>> agruparValidandoPorDia(Restaurante r, List<HorarioRequestDTO> tramos) {
        Map<DayOfWeek, List<HorarioRequestDTO>> porDia = new EnumMap<>(DayOfWeek.class);
        for (HorarioRequestDTO t : tramos) {
            DayOfWeek dia = require(t.getDia(), "Cada tramo debe tener 'dia'");
            porDia.computeIfAbsent(dia, k -> new ArrayList<>()).add(t);
        }

        Map<DayOfWeek, List<Horario>> resultado = new EnumMap<>(DayOfWeek.class);

        for (var entry : porDia.entrySet()) {
            DayOfWeek dia = entry.getKey();
            List<HorarioRequestDTO> slots = entry.getValue();

            List<Horario> parsed = new ArrayList<>();
            for (HorarioRequestDTO s : slots) {
                LocalTime a = parseHora(require(s.getApertura(), "apertura obligatoria"));
                LocalTime c = parseHora(require(s.getCierre(),   "cierre obligatorio"));
                validarTramoBasico(a, c);
                parsed.add(Horario.builder()
                        .restaurante(r)
                        .dia(dia)
                        .apertura(a)
                        .cierre(c)
                        .build());
            }

            parsed.sort(Comparator.comparing(Horario::getApertura).thenComparing(Horario::getCierre));
            validarListaSinSolapes(parsed);

            resultado.put(dia, parsed);
        }
        return resultado;
    }

    private void validarTramoBasico(LocalTime apertura, LocalTime cierre) {
        // sin cruzar medianoche y inicio<fin
        if (!apertura.isBefore(cierre)) {
            throw new IllegalArgumentException("apertura debe ser estrictamente anterior a cierre (no se admite cruce de medianoche)");
        }
    }

    /** Valida que (apertura,cierre) no solape con existentes. */
    private void validarSinSolapes(LocalTime apertura, LocalTime cierre, List<Horario> existentes, Integer selfId) {
        for (Horario h : existentes) {
            if (selfId != null && Objects.equals(h.getId(), selfId)) continue;
            // solape si: a < cierreExistente && cierre > aperturaExistente
            if (apertura.isBefore(h.getCierre()) && cierre.isAfter(h.getApertura())) {
                throw new IllegalArgumentException(
                        String.format("Tramo solapa con %s-%s",
                                h.getApertura().format(HM), h.getCierre().format(HM)));
            }
        }
    }

    /** Valida que una lista del mismo día esté libre de solapes. */
    private void validarListaSinSolapes(List<Horario> delDia) {
        for (int i = 1; i < delDia.size(); i++) {
            Horario prev = delDia.get(i - 1);
            Horario cur  = delDia.get(i);
            if (cur.getApertura().isBefore(prev.getCierre())) {
                throw new IllegalArgumentException(
                        String.format("Solape en %s: %s-%s con %s-%s",
                                prev.getDia(),
                                prev.getApertura().format(HM), prev.getCierre().format(HM),
                                cur.getApertura().format(HM),   cur.getCierre().format(HM)));
            }
        }
    }

    private LocalTime parseHora(String hhmm) {
        try {
            return LocalTime.parse(hhmm, HM);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de hora no válido, usa HH:mm → recibido: " + hhmm);
        }
    }

    private <T> T require(T val, String msg) {
        if (val == null) throw new IllegalArgumentException(msg);
        return val;
    }

    private HorarioDTO toDTO(Horario h) {
        return HorarioDTO.builder()
                .id(h.getId())
                .dia(h.getDia())
                .apertura(h.getApertura().format(HM))
                .cierre(h.getCierre().format(HM))
                .build();
    }
}
