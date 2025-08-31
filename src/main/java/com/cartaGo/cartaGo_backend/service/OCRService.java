package com.cartaGo.cartaGo_backend.service;

import com.google.cloud.vision.v1.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OCRService {

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
}
