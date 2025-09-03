package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO.AlergenoDTO;
import com.cartaGo.cartaGo_backend.entity.Alergeno;
import com.cartaGo.cartaGo_backend.repository.AlergenoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlergenosService {

    private final AlergenoRepository alergenoRepository;

    @Transactional(readOnly = true)
    public List<AlergenoDTO> listarAlergenos() {
        return alergenoRepository.findAll().stream()
                .sorted(Comparator.comparing(Alergeno::getNombre))
                .map(a -> AlergenoDTO.builder()
                        .id(a.getId()).nombre(a.getNombre()).imagen(a.getImagen())
                        .build())
                .toList();
    }

}
