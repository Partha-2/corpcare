package com.corpcare.pdfsplit.service.image;

import com.corpcare.pdfsplit.model.response.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageAnalyzerService {

    private static final String OUTPUT_DIR = "analyze-output/";
    private static final String BASE_URL = "/api/pdf";

    private final ImageExtractorService extractor;
    private final ImageTypeDetectorService typeDetector;
    private final ImageValueExtractor valueExtractor;
    private final ObjectMapper mapper = new ObjectMapper();

    public List<AnalysisResult> analyze(PDFRenderer renderer, PDDocument document,
                                         List<Integer> imagePages, String fileName) throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_DIR));
        List<AnalysisResult> results = new ArrayList<>();

        for (int page : imagePages) {
            String text = extractor.extract(renderer, page);
            if (text.trim().isEmpty()) continue;

            String type = typeDetector.detect(text);
            String confidence = typeDetector.confidence(text, type);
            Map<String, String> values = valueExtractor.extract(text, type);

            String baseName = type + "_page" + (page + 1) + "_" + LocalDate.now();
            String jsonFile = saveJson(baseName, type, page, confidence, "TESSERACT", values);
            String pdfFile = saveImageAsPdf(document, page, baseName);

            AnalysisResult result = new AnalysisResult();
            result.setType(type);
            result.setPageNumber(page + 1);
            result.setConfidence(confidence);
            result.setOcrEngine("TESSERACT");
            result.setValues(values);
            result.setValuesExtracted(values.size());
            result.setJsonFile(jsonFile);
            result.setPdfFile(pdfFile);
            result.setViewUrl(BASE_URL + "/analyze/view/" + jsonFile);
            result.setDownloadUrl(BASE_URL + "/analyze/download/" + pdfFile);
            result.setDeleteUrl(BASE_URL + "/analyze/delete/" + jsonFile);
            results.add(result);
        }
        return results;
    }

    public List<AnalysisResult> getAll() {
        List<AnalysisResult> results = new ArrayList<>();
        File folder = new File(OUTPUT_DIR);
        if (!folder.exists()) return results;
        File[] files = folder.listFiles();
        if (files == null) return results;

        Arrays.stream(files).filter(f -> f.getName().endsWith(".json")).forEach(f -> {
            try {
                AnalysisResult r = mapper.readValue(f, AnalysisResult.class);
                String base = f.getName().replace(".json", "");
                r.setViewUrl(BASE_URL + "/analyze/view/" + f.getName());
                r.setDownloadUrl(BASE_URL + "/analyze/download/" + base + ".pdf");
                r.setDeleteUrl(BASE_URL + "/analyze/delete/" + f.getName());
                results.add(r);
            } catch (Exception ignored) {}
        });
        return results;
    }

    public AnalysisResult getOne(String jsonFile) throws IOException {
        File file = new File(OUTPUT_DIR + jsonFile);
        return file.exists() ? mapper.readValue(file, AnalysisResult.class) : null;
    }

    public File getPdfFile(String pdfFileName) {
        File file = new File(OUTPUT_DIR + pdfFileName);
        return file.exists() ? file : null;
    }

    public boolean delete(String jsonFile) {
        boolean deleted = new File(OUTPUT_DIR + jsonFile).delete();
        new File(OUTPUT_DIR + jsonFile.replace(".json", ".pdf")).delete();
        return deleted;
    }

    private String saveJson(String baseName, String type, int page,
                            String confidence, String engine, Map<String, String> values) throws IOException {
        AnalysisResult r = new AnalysisResult();
        r.setType(type);
        r.setPageNumber(page + 1);
        r.setConfidence(confidence);
        r.setOcrEngine(engine);
        r.setValues(values);
        r.setValuesExtracted(values.size());
        String jsonName = baseName + ".json";
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(OUTPUT_DIR + jsonName), r);
        return jsonName;
    }

    private String saveImageAsPdf(PDDocument document, int pageIndex, String baseName) throws IOException {
        PDDocument out = new PDDocument();
        out.addPage(document.getPage(pageIndex));
        String pdfName = baseName + ".pdf";
        out.save(OUTPUT_DIR + pdfName);
        out.close();
        return pdfName;
    }
}
