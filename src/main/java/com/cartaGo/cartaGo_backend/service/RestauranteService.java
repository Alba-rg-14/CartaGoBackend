package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.CartaDTO.CartaDTO;
import com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO.PlatoDTO;
import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioDTO;
import com.cartaGo.cartaGo_backend.dto.RestauranteDTO.RestauranteDTO;
import com.cartaGo.cartaGo_backend.dto.RestauranteDTO.RestaurantePreviewDTO;
import com.cartaGo.cartaGo_backend.dto.RestauranteDTO.RestauranteUpdateDTO;
import com.cartaGo.cartaGo_backend.dto.UsuarioLoginDTO.ClienteDTO;
import com.cartaGo.cartaGo_backend.entity.Carta;
import com.cartaGo.cartaGo_backend.entity.Cliente;
import com.cartaGo.cartaGo_backend.entity.Restaurante;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.CartaRepository;
import com.cartaGo.cartaGo_backend.repository.HorarioRepository;
import com.cartaGo.cartaGo_backend.repository.RestauranteRepository;
import com.cartaGo.cartaGo_backend.utils.GeoUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.List;

import static com.cartaGo.cartaGo_backend.mapper.RestauranteMapper.toDTO;

@Service
@RequiredArgsConstructor
public class RestauranteService {
    private final RestauranteRepository restauranteRepository;
    private final GeocodingService geocodingService;
    private final HorarioRepository horarioRepository;
    private final CartaRepository cartaRepository;
    private final PlatoService platoService;
    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private final ZonedDateTime ahora = java.time.ZonedDateTime.now(ZONA);


    //Crear restaurante dado un usuario y nombre
    public Restaurante crearRestaurante(Usuario usuario, String nombre){
        Restaurante r = Restaurante.builder()
                .nombre(nombre)
                .usuario(usuario)
                .build();
        restauranteRepository.saveAndFlush(r);
        return r;
    }

    public Restaurante.Estado getEstadoRestaurante(Integer restauranteId){
        Restaurante r = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + restauranteId));

        DayOfWeek dia = ahora.getDayOfWeek();
        LocalTime hora = ahora.toLocalTime();

        boolean abierto = horarioRepository
                .findByRestauranteIdAndDiaOrderByAperturaAsc(restauranteId, dia)
                .stream()
                .anyMatch(h -> !hora.isBefore(h.getApertura()) && hora.isBefore(h.getCierre()));

        r.setEstado(abierto ? Restaurante.Estado.abierto : Restaurante.Estado.cerrado);
        restauranteRepository.save(r);
        return r.getEstado();
    }

    public List<RestauranteDTO> getAllRestaurantes() {
       return restauranteRepository.findAll().stream()
               .map(this::mapToDto) .toList();
    }

    public List<RestaurantePreviewDTO> getAllRestaurantesPreviewDTO(){
        return restauranteRepository.findAll().stream()
                .map(this::mapToPreviewDto).toList();

    }

    public List<RestaurantePreviewDTO> getRestaurantesPreviewDTOByNombre(String name) {
        String filtro = name == null ? "" : name.trim();
        return restauranteRepository.findAllByNombreContainingIgnoreCase(filtro).stream()
                .map(this::mapToPreviewDto)
                .toList(); // Java 17+: OK
    }


    public RestauranteDTO getRestauranteInfoById(Integer restauranteId){
        return restauranteRepository.findById(restauranteId)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + restauranteId));
    }


    public RestaurantePreviewDTO getById(Integer id) {
        return restauranteRepository.findById(id).map(this::mapToPreviewDto).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));
    }

    public List<RestaurantePreviewDTO> getRestaurantesPreviewDTOAbiertos() {

        DayOfWeek dia = ahora.getDayOfWeek();
        LocalTime hora = ahora.toLocalTime();


        return restauranteRepository.findAll().stream()
                .filter(r -> horarioRepository
                        .findByRestauranteIdAndDiaOrderByAperturaAsc(r.getId(), dia)
                        .stream()
                        .anyMatch(h -> !hora.isBefore(h.getApertura()) && hora.isBefore(h.getCierre())))
                .map(this::mapToPreviewDto)
                .toList();
    }

    public List<RestauranteDTO> findCercanos(double userLat, double userLon, double radioKm) {
        // 1) Bounding box para reducir candidatos
        double dLat = GeoUtils.deltaLatFor(radioKm);
        double dLon = GeoUtils.deltaLonFor(userLat, radioKm);

        double minLat = userLat - dLat;
        double maxLat = userLat + dLat;
        double minLon = userLon - dLon;
        double maxLon = userLon + dLon;

        // 2) Filtrar finamente por Haversine y mapear a DTO
        return restauranteRepository.findByLatBetweenAndLonBetween(minLat, maxLat, minLon, maxLon).stream()
                .filter(r -> GeoUtils.haversineKm(userLat, userLon, r.getLat(), r.getLon()) <= radioKm)
                .sorted((a, b) -> {
                    double da = GeoUtils.haversineKm(userLat, userLon, a.getLat(), a.getLon());
                    double db = GeoUtils.haversineKm(userLat, userLon, b.getLat(), b.getLon());
                    return Double.compare(da, db);
                })
                .map(this::mapToDto)
                .toList();
    }

    // por defecto radio 1km
    public List<RestauranteDTO> findCercanos1Km(double userLat, double userLon) {
        return findCercanos(userLat, userLon, 1.0);
    }

    public void setRestauranteUbicacion(Integer id, String dir) throws Exception{
        Double lat, lon;
        Restaurante r = restauranteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));
        r.setDireccion(dir);
        var res= geocodingService.buscarPorDireccion(dir);
        if (null != res){
            r.setLat(res.getLat() );
            r.setLon(res.getLon());
            restauranteRepository.saveAndFlush(r);
        }else {
            throw new Exception("No se ha encontrado la dirección:" + dir) ;
        }
    }

    public void setRestauranteCoor(Integer id, Double lat, Double lon){
        Restaurante r = restauranteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));
        r.setLat(lat);
        r.setLon(lon);
        var res = geocodingService.buscarPorCoordenadas(lat,lon);
        r.setDireccion(res.getDireccion());
        restauranteRepository.saveAndFlush(r);
    }


    private RestauranteDTO mapToDto(Restaurante r) {
        // 1) calcular estado "abierto/cerrado" AHORA (día y hora actuales en nuestra zona horaria)

        var dia   = ahora.getDayOfWeek();
        var hora  = ahora.toLocalTime();

        boolean abierto = horarioRepository
                .findByRestauranteIdAndDiaOrderByAperturaAsc(r.getId(), dia)
                .stream()
                .anyMatch(h -> !hora.isBefore(h.getApertura()) && hora.isBefore(h.getCierre()));

        // 2) montar lista de horarios (toda la semana) formateando HH:mm
        var HM = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        var horariosDto = horarioRepository
                .findByRestauranteIdOrderByDiaAscAperturaAsc(r.getId())
                .stream()
                .map(h -> com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioDTO.builder()
                        .id(h.getId())
                        .dia(h.getDia())
                        .apertura(h.getApertura().format(HM))
                        .cierre(h.getCierre().format(HM))
                        .build()
                )
                .toList();

        // 3) devolver RestauranteDTO con estado calculado + horarios
        return com.cartaGo.cartaGo_backend.dto.RestauranteDTO.RestauranteDTO.builder()
                .id(r.getId())
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .imagen(r.getImagen())
                .estado(abierto ? com.cartaGo.cartaGo_backend.entity.Restaurante.Estado.abierto
                        : com.cartaGo.cartaGo_backend.entity.Restaurante.Estado.cerrado)
                .direccion(r.getDireccion())
                .lat(r.getLat())
                .lon(r.getLon())
                .horarios(horariosDto)
                .build();
    }


    public RestaurantePreviewDTO mapToPreviewDto(Restaurante r){
        return RestaurantePreviewDTO.builder()
                .id(r.getId())
                .nombre(r.getNombre())
                .imagen(r.getImagen())
                .build();
    }

    public void cambiarEstado(Integer id) {
        Restaurante r = restauranteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));

        if((Restaurante.Estado.abierto).equals(r.getEstado())){
            r.setEstado(Restaurante.Estado.cerrado);
        }else if ((Restaurante.Estado.cerrado).equals(r.getEstado())){
            r.setEstado(Restaurante.Estado.abierto);
        }else{
            r.setEstado(Restaurante.Estado.abierto);
        }
        restauranteRepository.saveAndFlush(r);
    }

    public String setImagen(Integer id, String url) {
        String u = url == null ? null : url.trim();
        if (u == null || !u.startsWith("https://res.cloudinary.com/")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "URL de imágen inválida");
        }
        Restaurante r = restauranteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));
        r.setImagen(u);
        restauranteRepository.saveAndFlush(r);
        return u;
    }

    public String getImagen(Integer id) {
        var r = restauranteRepository.findById(id).orElseThrow();
        return r.getImagen() == null ? "" : r.getImagen();
    }

    public void deleteImagen(Integer id) {
        var r = restauranteRepository.findById(id).orElseThrow();
        r.setImagen(null);
        restauranteRepository.saveAndFlush(r);
    }

    // Crear carta para un restaurante (1:1)
    @Transactional
    public void crearCarta(Integer id) {
        Restaurante r = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado con id: " + id));

        if (r.getCarta() != null) throw new IllegalStateException("Ya tiene carta");

        Carta c = new Carta();
        r.setCarta(c);              // enlaza ambos lados (método en la entity)
        // No hace falta llamar a save(c): cascade PERSIST desde Restaurante -> Carta
        // Tampoco a save(r) si r está managed; pero se puede por claridad:
        restauranteRepository.saveAndFlush(r);
    }

    // Reemplazar carta (borra la anterior por orphanRemoval y pone la nueva)
    @Transactional
    public void reemplazarCarta(Integer id) {
        Restaurante r = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado con id: " + id));
        Carta nueva = new Carta();

        r.setCarta(nueva); // el setter en la entity rompe el enlace antiguo y pone el nuevo
        restauranteRepository.saveAndFlush(r); // opcional si r sigue managed; explícito está bien
    }

    // Quitar (y borrar) la carta
    @Transactional
    public void quitarCarta(Integer id) {
        Restaurante r = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado con id: " + id));

        r.removeCarta();            // método en la entity
        restauranteRepository.saveAndFlush(r);    // al hacer flush/commit, orphanRemoval elimina la carta
    }

    @Transactional(readOnly = true)
    public List<PlatoDTO> obtenerCarta(Integer restauranteId) {
        return platoService.listarPorCarta(cartaRepository.findByRestauranteId(restauranteId).get().getId(), null);
    }

    @Transactional
    public RestauranteDTO actualizarRestaurante(Integer id, RestauranteUpdateDTO req) {
        Restaurante r = restauranteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado"));

        if (req.getNombre() != null)      r.setNombre(req.getNombre().trim());
        if (req.getDescripcion() != null) r.setDescripcion(req.getDescripcion().trim());
        if (req.getDireccion() != null)   r.setDireccion(req.getDireccion().trim());
        if (req.getLat() != null)         r.setLat(req.getLat());
        if (req.getLon() != null)         r.setLon(req.getLon());

        if (req.getImagen() != null) {
            String u = req.getImagen().trim();
            if (!u.isEmpty() && !u.startsWith("https://res.cloudinary.com/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL de imagen inválida");
            }
            r.setImagen(u.isEmpty() ? null : u);
        }

        if (req.getEstado() != null) {
            r.setEstado(Restaurante.Estado.valueOf(req.getEstado().toLowerCase()));
        }

        Restaurante saved = restauranteRepository.saveAndFlush(r);
        return toDTO(saved);
    }


    public Integer getCartaIdByRestauranteId(Integer restauranteId) {
        Restaurante r = restauranteRepository.findById(restauranteId).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado"));
        return r.getCarta().getId();
    }

    @Transactional
    public void actualizarEstadoSegunHorario(Integer restauranteId) {
        Restaurante r = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + restauranteId));


        DayOfWeek dia = ahora.getDayOfWeek();
        LocalTime hora = ahora.toLocalTime();

        boolean abierto = horarioRepository
                .findByRestauranteIdAndDiaOrderByAperturaAsc(restauranteId, dia)
                .stream()
                .anyMatch(h -> !hora.isBefore(h.getApertura()) && hora.isBefore(h.getCierre()));

        r.setEstado(abierto ? Restaurante.Estado.abierto : Restaurante.Estado.cerrado);
        restauranteRepository.save(r);
    }

    public RestauranteDTO getByUsuarioId(Integer usuarioId) {
        Restaurante r = restauranteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe restaurante asociado al usuario " + usuarioId
                ));
        return RestauranteDTO.from(r);
    }
}
