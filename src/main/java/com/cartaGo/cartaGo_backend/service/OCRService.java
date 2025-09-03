package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.APIsDTO.OcrScanResponseDTO;
import com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO.PlatoDTO;
import com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO.PlatoRequestDTO;
import com.cartaGo.cartaGo_backend.entity.Carta;
import com.cartaGo.cartaGo_backend.repository.CartaRepository;
import com.google.cloud.vision.v1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OCRService {

    private final RestauranteService restauranteService;
    private final PlatoService platoService;
    private final CartaRepository cartaRepository;
    private final OcrMenuParserService parser = new OcrMenuParserService();

    public List<String> extractTextFromUrls(List<String> imageUrls) throws IOException {
        List<String> results = new ArrayList<>();

        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            List<AnnotateImageRequest> requests = new ArrayList<>();

            for (String url : imageUrls) {
                ImageSource imgSource = ImageSource.newBuilder().setImageUri(url).build();
                Image img = Image.newBuilder().setSource(imgSource).build();
                Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(feat)
                        .setImage(img)
                        .build();
                requests.add(request);
            }

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    results.add("Error: " + res.getError().getMessage());
                } else {
                    results.add(res.getFullTextAnnotation().getText());
                }
            }
        }

        return results;
    }

    /** De cero: reemplaza la carta del restaurante y crea platos nuevos a partir de los textos OCR */
    public List<PlatoDTO> importReplace(Integer restauranteId, List<String> textosOcr) throws IOException {
        restauranteService.reemplazarCarta(restauranteId);

        Integer cartaId = restauranteService.getCartaIdByRestauranteId(restauranteId);
        if (cartaId == null) {
            throw new IllegalStateException("No se pudo obtener cartaId tras reemplazar la carta.");
        }

        return parseAndCreatePlatos(cartaId, textosOcr);
    }

    /** Añadir a lo existente: si no hay carta, la crea; luego añade platos parseados */
    public List<PlatoDTO> importAppend(Integer restauranteId, List<String> textosOcr) throws IOException {
        Integer cartaId = restauranteService.getCartaIdByRestauranteId(restauranteId);
        if (cartaId == null) {
            restauranteService.crearCarta(restauranteId);
            cartaId = restauranteService.getCartaIdByRestauranteId(restauranteId);
        }

        return parseAndCreatePlatos(cartaId, textosOcr);
    }

    // ========== helpers internos ==========

    private List<PlatoDTO> parseAndCreatePlatos(Integer cartaId, List<String> textosOcr) {
        List<PlatoDTO> creados = new ArrayList<>();

        for (String text : textosOcr) {
            List<PlatoRequestDTO> platos = parser.parse(text);
            for (PlatoRequestDTO req : platos) {
                if (req.getSeccion() == null || req.getSeccion().isBlank()) {
                    req.setSeccion("General");
                }
                creados.add(platoService.crearPlato(cartaId, req));
            }
        }
        return creados;
    }

    @Transactional
    public OcrScanResponseDTO scanAndImport(Integer restauranteId, List<String> imageUrls, String modeRaw) {
        String mode = (modeRaw == null) ? "replace" : modeRaw.trim().toLowerCase();

        // 1) OCR → bloque de texto por cada imagen
        List<String> ocrBlocks;
        try {
            ocrBlocks = extractTextFromUrls(imageUrls);
        } catch (IOException e) {
            throw new RuntimeException("Error llamando a Google Vision OCR", e);
        }

        // 2) Parsear todo
        List<PlatoRequestDTO> platosParsed = new ArrayList<>();
        for (String block : ocrBlocks) {
            platosParsed.addAll(parser.parse(block));
        }

        // 3) Asegurar carta (replace o append)
        Carta carta = ensureCartaAccordingMode(restauranteId, mode);

        // 4) Guardar platos en BBDD y recoger los PlatoDTO resultantes
        List<PlatoDTO> creados = new ArrayList<>();
        for (PlatoRequestDTO p : platosParsed) {
            PlatoDTO dto = platoService.crearPlato(carta.getId(), p);
            creados.add(dto);
        }

        // 5) Rellenar y devolver DTO de respuesta
        return new OcrScanResponseDTO(restauranteId, creados.size(), creados);
    }

    private Carta ensureCartaAccordingMode(Integer restauranteId, String modeRaw) {
        String mode = (modeRaw == null) ? "replace" : modeRaw.trim().toLowerCase(Locale.ROOT);

        switch (mode) {
            case "append": {
                // Si no existe carta, créala
                Optional<Carta> maybe = cartaRepository.findByRestauranteId(restauranteId);
                if (maybe.isEmpty()) {
                    restauranteService.crearCarta(restauranteId);
                }
                break;
            }
            case "replace":
            default: {
                // Reemplaza carta (borra la antigua y crea una nueva)
                restauranteService.reemplazarCarta(restauranteId);
                break;
            }
        }

        // Devuelve la carta actual (la nueva o la existente)
        return cartaRepository.findByRestauranteId(restauranteId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo obtener la carta del restaurante " + restauranteId));
    }


}
