package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.UsuarioResponseDTO;
import com.cartaGo.cartaGo_backend.entity.Cliente;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.ClienteRepository;
import com.cartaGo.cartaGo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


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
}
