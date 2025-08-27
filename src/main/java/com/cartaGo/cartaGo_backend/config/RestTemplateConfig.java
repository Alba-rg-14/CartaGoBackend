package com.cartaGo.cartaGo_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(@Value("${geocode.mapsco.timeout-ms}") int timeoutMs) {
        // Timeouts
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        RestTemplate rt = new RestTemplate(factory);

        // Cabecera User-Agent (algunos servicios la exigen)
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "CartaGo/1.0 (+https://example.com)");
            return execution.execute(request, body);
        });
        rt.setInterceptors(interceptors);

        return rt;
    }
}
