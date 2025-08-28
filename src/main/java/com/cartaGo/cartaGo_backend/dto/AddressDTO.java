package com.cartaGo.cartaGo_backend.dto;

public record AddressDTO (

    String road,          // calle
    String houseNumber,   // nº
    String neighbourhood, // barrio (opcional)
    String borough,       // distrito (opcional)
    String city,          // ciudad/pueblo
    String province,      // provincia
    String state,         // comunidad / estado
    String postcode,      // CP
    String country,       // país
    String countryCode    // es, fr, etc.
)
{}
