package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.entity.Carta;
import com.cartaGo.cartaGo_backend.repository.CartaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartaService {

    private final CartaRepository cartaRepository;

    @Transactional(readOnly = true)
    public Carta findById(Integer id) {
        return cartaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Carta no encontrada"));
    }
}
