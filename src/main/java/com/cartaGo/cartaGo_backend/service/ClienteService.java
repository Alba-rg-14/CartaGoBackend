package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.UsuarioLoginDTO.ClienteDTO;
import com.cartaGo.cartaGo_backend.entity.Cliente;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    //Crear cliente dado un usuario y nombre
    public Cliente crearCliente(Usuario usuario, String nombre){
        Cliente c = Cliente.builder()
                .nombre(nombre)
                .usuario(usuario)
                .build();
        clienteRepository.saveAndFlush(c);
        return c;
    }

    @Transactional(readOnly = true)
    public ClienteDTO getByUsuarioId(Integer usuarioId) {
        Cliente c = clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe cliente asociado al usuario " + usuarioId
                ));
        return ClienteDTO.from(c);
    }
}
