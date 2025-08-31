package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.service.OCRService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

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
}

