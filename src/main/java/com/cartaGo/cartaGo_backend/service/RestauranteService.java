package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.RestauranteDTO;
import com.cartaGo.cartaGo_backend.dto.RestaurantePreviewDTO;
import com.cartaGo.cartaGo_backend.entity.Restaurante;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.RestauranteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestauranteService {
    private final RestauranteRepository restauranteRepository;

    //Crear restaurante dado un usuario y nombre
    public Restaurante crearRestaurante(Usuario usuario, String nombre){
        Restaurante r = Restaurante.builder()
                .nombre(nombre)
                .usuario(usuario)
                .build();
        restauranteRepository.saveAndFlush(r);
        return r;
    }

    public List<RestauranteDTO> getAllRestaurantes() {
       return restauranteRepository.findAll().stream()
               .map(this::mapToDto) .toList();
    }

    public List<RestaurantePreviewDTO> getAllRestaurantesPreviewDTO(){
        return restauranteRepository.findAll().stream()
                .map(this::mapToPreviewDto).toList();

    }

    public RestauranteDTO getRestaurantesPreviewDTOByNombre(String name){

        return restauranteRepository.findByNombreContainingIgnoreCase(name)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con nombre: " + name));

    }


    public RestaurantePreviewDTO getById(Integer id) {
        return restauranteRepository.findById(id).map(this::mapToPreviewDto).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));
    }

    public List<RestaurantePreviewDTO> getRestaurantesPreviewDTOAbiertos() {
        return restauranteRepository.findByEstado(Restaurante.Estado.abierto).stream()
                .map(this::mapToPreviewDto).toList();
    }

    private RestauranteDTO mapToDto(Restaurante r) {
        return RestauranteDTO.builder()
                .id(r.getId())
                .descripcion(r.getDescripcion())
                .nombre(r.getNombre())
                .imagen(r.getImagen())
                .estado(r.getEstado())
                .direccion(r.getDireccion())
                .lat(r.getLat())
                .lon(r.getLon())
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
}
