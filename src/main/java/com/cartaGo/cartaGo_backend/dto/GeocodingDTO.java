package com.cartaGo.cartaGo_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeocodingDTO(
        String displayName,  // línea “bonita”
        Double lat,
        Double lon,
        AddressDTO address   // dirección desglosada
) { }
