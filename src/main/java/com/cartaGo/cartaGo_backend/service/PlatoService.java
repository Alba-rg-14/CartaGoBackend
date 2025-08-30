package com.cartaGo.cartaGo_backend.service;


import ch.qos.logback.classic.Logger;
import com.cartaGo.cartaGo_backend.dto.AlergenoDTO;
import com.cartaGo.cartaGo_backend.dto.PlatoDTO;
import com.cartaGo.cartaGo_backend.dto.PlatoRequestDTO;
import com.cartaGo.cartaGo_backend.entity.Alergeno;
import com.cartaGo.cartaGo_backend.entity.Carta;
import com.cartaGo.cartaGo_backend.entity.Plato;
import com.cartaGo.cartaGo_backend.entity.PlatoAlergenos;
import com.cartaGo.cartaGo_backend.repository.AlergenoRepository;
import com.cartaGo.cartaGo_backend.repository.CartaRepository;
import com.cartaGo.cartaGo_backend.repository.PlatoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;


@Service
    @RequiredArgsConstructor
    public class PlatoService {

        private final PlatoRepository platoRepository;
        private final CartaRepository cartaRepository;
        private final AlergenoRepository alergenoRepository;

        // ===== CREATE =====
        @Transactional
        public PlatoDTO crearPlato(Integer cartaId, PlatoRequestDTO req) {
            Carta carta = cartaRepository.findById(cartaId)
                    .orElseThrow(() -> new EntityNotFoundException("Carta no encontrada con id: " + cartaId));

            Plato p = new Plato();
            applyRequestToEntity(p, req, /*isCreate=*/true);

            // vincular carta
            p.setCarta(carta);

            // orden: si viene null, lo autocalculamos al final de su sección
            if (req.getOrden() == null) {
                Integer max = platoRepository.findMaxOrdenByCartaAndSeccion(carta.getId(), p.getSeccion());
                p.setOrden((max == null ? 0 : max) + 1);
            }

            // alergenos
            replaceAlergenos(p, req.getAlergenosIds());

            Plato saved = platoRepository.saveAndFlush(p);
            return toDTO(saved);
        }

        // ===== READ (uno) =====
        @Transactional(readOnly = true)
        public PlatoDTO obtenerPlato(Integer platoId) {
            Plato p = platoRepository.findById(platoId)
                    .orElseThrow(() -> new EntityNotFoundException("Plato no encontrado con id: " + platoId));
            return toDTO(p);
        }

        // ===== LIST por carta (opcional filtrar por seccion) =====
        @Transactional(readOnly = true)
        public List<PlatoDTO> listarPorCarta(Integer cartaId, String seccion) {
            List<Plato> platos = (seccion == null || seccion.isBlank())
                    ? platoRepository.findByCarta_IdOrderBySeccionAscOrdenAsc(cartaId)
                    : platoRepository.findByCarta_IdAndSeccionOrderByOrdenAsc(cartaId, seccion);

            return platos.stream().map(this::toDTO).toList();
        }

        // ===== UPDATE =====
        @Transactional
        public PlatoDTO actualizarPlato(Integer platoId, PlatoRequestDTO req) {
            Plato p = platoRepository.findById(platoId)
                    .orElseThrow(() -> new EntityNotFoundException("Plato no encontrado con id: " + platoId));

            // Si cambia la sección y no nos pasan orden, recolocamos al final de la nueva sección
            boolean cambiaSeccion = req.getSeccion() != null && !req.getSeccion().equals(p.getSeccion());

            applyRequestToEntity(p, req, /*isCreate=*/false);

            if (cambiaSeccion && req.getOrden() == null) {
                Integer max = platoRepository.findMaxOrdenByCartaAndSeccion(p.getCarta().getId(), p.getSeccion());
                p.setOrden((max == null ? 0 : max) + 1);
            }

            // alergenos
            replaceAlergenos(p, req.getAlergenosIds());

            Plato saved = platoRepository.saveAndFlush(p);
            return toDTO(saved);
        }

        // ===== DELETE =====
        @Transactional
        public void eliminarPlato(Integer platoId) {
            Plato p = platoRepository.findById(platoId)
                    .orElseThrow(() -> new EntityNotFoundException("Plato no encontrado con id: " + platoId));
            platoRepository.delete(p);
        }

        // ===== Imagen (Cloudinary-like) =====
        @Transactional
        public String setImagen(Integer platoId, String url) {
            String u = (url == null ? null : url.trim());
            if (u == null || !u.startsWith("https://res.cloudinary.com/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL de imagen inválida");
            }
            Plato p = platoRepository.findById(platoId)
                    .orElseThrow(() -> new EntityNotFoundException("Plato no encontrado con id: " + platoId));
            p.setImagen(u);
            platoRepository.saveAndFlush(p);
            return u;
        }

        @Transactional(readOnly = true)
        public String getImagen(Integer platoId) {
            Plato p = platoRepository.findById(platoId)
                    .orElseThrow(() -> new EntityNotFoundException("Plato no encontrado con id: " + platoId));
            return p.getImagen() == null ? "" : p.getImagen();
        }

        @Transactional
        public void deleteImagen(Integer platoId) {
            Plato p = platoRepository.findById(platoId)
                    .orElseThrow(() -> new EntityNotFoundException("Plato no encontrado con id: " + platoId));
            p.setImagen(null);
            platoRepository.saveAndFlush(p);
        }

        // ===== Helpers =====

        private void applyRequestToEntity(Plato p, PlatoRequestDTO req, boolean isCreate) {
            if (req.getNombre() != null)        p.setNombre(req.getNombre());
            if (req.getDescripcion() != null)   p.setDescripcion(req.getDescripcion());
            if (req.getPrecio() != null)        p.setPrecio(req.getPrecio());
            if (req.getSeccion() != null)       p.setSeccion(req.getSeccion());
            if (req.getOrden() != null)         p.setOrden(req.getOrden());
            if (req.getImagen() != null) {
                String u = req.getImagen().trim();
                if (!u.isEmpty() && !u.startsWith("https://res.cloudinary.com/")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL de imagen inválida");
                }
                p.setImagen(u.isEmpty() ? null : u);
            }
            // Nota: el id del request NO se mapea a la entity
            if (!isCreate && req.getId() != null && !req.getId().equals(p.getId())) {
                // si llega un id que no coincide, lo ignoramos o lanzamos error
            }
        }

        private PlatoDTO toDTO(Plato p) {
            List<AlergenoDTO> alers = new ArrayList<>();
            if (p.getAlergenos() != null) {
                for (PlatoAlergenos pa : p.getAlergenos()) {
                    Alergeno a = pa.getAlergeno();
                    alers.add(AlergenoDTO.builder()
                            .id(a.getId())
                            .nombre(a.getNombre())
                            .imagen(a.getImagen())
                            .build());
                }
            }
            return PlatoDTO.builder()
                    .id(p.getId())
                    .nombre(p.getNombre())
                    .descripcion(p.getDescripcion())
                    .precio(p.getPrecio())
                    .seccion(p.getSeccion())
                    .orden(p.getOrden())
                    .imagen(p.getImagen())
                    .alergenos(alers)
                    .build();
        }

        private void replaceAlergenos(Plato p, List<Integer> alergenoIds) {
            if (alergenoIds == null) return; // no tocar
            // limpiamos y reconstruimos
            if (p.getAlergenos() == null) {
                p.setAlergenos(new ArrayList<>());
            } else {
                p.getAlergenos().clear(); // orphanRemoval elimina los antiguos
            }
            for (Integer aId : alergenoIds) {
                Alergeno a = alergenoRepository.findById(aId)
                        .orElseThrow(() -> new EntityNotFoundException("Alergeno no encontrado: " + aId));
                PlatoAlergenos pa = new PlatoAlergenos(); // <-- usa tu constructor real si lo tienes
                pa.setPlato(p);
                pa.setAlergeno(a);
                p.getAlergenos().add(pa);
            }
        }


    }
