package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.OcrScanRequestDTO;
import com.cartaGo.cartaGo_backend.dto.OcrScanResponseDTO;
import com.cartaGo.cartaGo_backend.dto.PlatoDTO;
import com.cartaGo.cartaGo_backend.service.OCRService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ocr")
public class OcrController {

    private final OCRService ocrService;

    public OcrController(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/extract")
    public List<String> extractText(@RequestBody List<String> urls) throws IOException {
        return ocrService.extractTextFromUrls(urls);
    }

    /**
     * A) Reemplaza carta + crea platos nuevos desde textos OCR
     * Body: ["texto ocr img1", "texto ocr img2", ...]
     */
    @PostMapping("/restaurante/{restauranteId}/replace")
    public Map<String, Object> importReplace(@PathVariable Integer restauranteId,
                                             @RequestBody List<String> textosOcr) throws IOException {
        List<PlatoDTO> creados = ocrService.importReplace(restauranteId, textosOcr);
        return Map.of(
                "restauranteId", restauranteId,
                "platosCreados", creados.size(),
                "detalles", creados
        );
    }

    /**
     * B) Añade platos a la carta existente (si no hay carta, la crea).
     * Body: ["texto ocr img1", "texto ocr img2", ...]
     */
    @PostMapping("/restaurante/{restauranteId}/append")
    public Map<String, Object> importAppend(@PathVariable Integer restauranteId,
                                            @RequestBody List<String> textosOcr) throws IOException {
        List<PlatoDTO> creados = ocrService.importAppend(restauranteId, textosOcr);
        return Map.of(
                "restauranteId", restauranteId,
                "platosCreados", creados.size(),
                "detalles", creados
        );
    }

    // POST /ocr/restaurante/2/scan?mode=replace   (mode opcional: si no viene, por defecto "replace")
    @PostMapping("/restaurante/{restauranteId}/scan")
    public ResponseEntity<OcrScanResponseDTO> scanAndImport(
            @PathVariable Integer restauranteId,
            @RequestBody OcrScanRequestDTO req,
            @RequestParam(value = "mode", required = false) String modeQuery
    ) {
        if (req == null || req.getImageUrls() == null || req.getImageUrls().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String effectiveMode = (modeQuery != null) ? modeQuery : req.getMode();
        OcrScanResponseDTO resp = ocrService.scanAndImport(restauranteId, req.getImageUrls(), effectiveMode);
        return ResponseEntity.ok(resp);
    }
}

