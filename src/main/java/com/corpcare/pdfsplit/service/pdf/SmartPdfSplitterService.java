package com.corpcare.pdfsplit.service.pdf;

import com.corpcare.pdfsplit.model.response.AnalysisResult;
import com.corpcare.pdfsplit.model.response.CombinedResult;
import com.corpcare.pdfsplit.model.response.SplitResult;
import com.corpcare.pdfsplit.service.image.ImageAnalyzerService;
import com.corpcare.pdfsplit.service.llm.LlmClassifierService;
import com.corpcare.pdfsplit.service.llm.LlmClassifierService.LlmResult;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SmartPdfSplitterService {

    private static final Logger log = LoggerFactory.getLogger(SmartPdfSplitterService.class);

    private static final String OUTPUT_DIR = "split-output/";
    private static final String BASE_URL = "/api/pdf";

    private final PdfValidatorService validator;
    private final PdfTextExtractor extractor;
    private final PdfCategoryDetector detector;
    private final ImageAnalyzerService imageAnalyzer;
    private final LlmClassifierService llmClassifier;

    public CombinedResult process(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        PDDocument document = validator.validateAndOpen(file);
        PDFRenderer renderer = new PDFRenderer(document);
        int totalPages = document.getNumberOfPages();

        Map<String, List<Integer>> buckets = new LinkedHashMap<>();
        buckets.put("lab", new ArrayList<>());
        buckets.put("eye", new ArrayList<>());
        buckets.put("chest", new ArrayList<>());

        List<Integer> imagePages = new ArrayList<>();

        for (int i = 0; i < totalPages; i++) {
            String text = extractor.extractPage(document, i);
            if (text.isEmpty()) {
                imagePages.add(i);
            } else {
                String rawCategory = detector.detectOne(text);
                String confidence = detector.confidence(text, rawCategory);
                if ("LOW".equals(confidence) && rawCategory != null && llmClassifier.isAvailable()) {
                    try {
                        LlmResult llmResult = llmClassifier.classifyText(text);
                        if (llmResult != null && !"UNKNOWN".equals(llmResult.getType())) {
                            log.info("LLM reclassified page {} from {} to {}", i + 1, rawCategory, llmResult.getType());
                            rawCategory = llmResult.getType().toLowerCase();
                        }
                    } catch (Exception e) {
                        log.warn("LLM reclassification failed for page {}: {}", i + 1, e.getMessage());
                    }
                }
                String category = detector.toFinal(rawCategory);
                buckets.get(category).add(i);
            }
        }

        List<SplitResult> splitResults = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : buckets.entrySet()) {
            splitResults.add(savePdf(document, entry.getKey(), entry.getValue()));
        }

        List<AnalysisResult> imageResults = imageAnalyzer.analyze(
                renderer, document, imagePages, file.getOriginalFilename());

        document.close();

        CombinedResult result = new CombinedResult();
        result.setSplitResults(splitResults);
        result.setImageResults(imageResults);
        return result;
    }

    private SplitResult savePdf(PDDocument document, String category, List<Integer> pages) throws IOException {
        SplitResult result = new SplitResult();
        result.setCategory(category);
        result.setFound(!pages.isEmpty());
        result.setPageCount(pages.size());

        if (pages.isEmpty()) {
            result.setFileName("not_found");
            return result;
        }

        PDDocument out = new PDDocument();
        for (int i : pages) out.addPage(document.getPage(i));

        String fileName = category + "_report.pdf";
        out.save(OUTPUT_DIR + fileName);
        out.close();

        result.setFileName(fileName);
        result.setDownloadUrl(BASE_URL + "/" + fileName + "/download");
        result.setViewUrl(BASE_URL + "/" + fileName + "/view");
        result.setDeleteUrl(BASE_URL + "/" + fileName);
        return result;
    }

    public List<SplitResult> getAllFiles() {
        List<SplitResult> files = new ArrayList<>();
        File folder = new File(OUTPUT_DIR);
        if (!folder.exists()) return files;

        for (File f : Objects.requireNonNull(folder.listFiles())) {
            if (!f.getName().endsWith(".pdf")) continue;
            int pageCount = 0;
            boolean ok = true;
            try {
                PDDocument doc = Loader.loadPDF(f);
                pageCount = doc.getNumberOfPages();
                doc.close();
            } catch (IOException e) {
                ok = false;
            }
            SplitResult r = new SplitResult();
            r.setCategory(f.getName().replace("_report.pdf", ""));
            r.setPageCount(pageCount);
            r.setFound(ok);
            r.setFileName(ok ? f.getName() : f.getName() + " (corrupted)");
            if (ok) {
                r.setDownloadUrl(BASE_URL + "/" + f.getName() + "/download");
                r.setViewUrl(BASE_URL + "/" + f.getName() + "/view");
                r.setDeleteUrl(BASE_URL + "/" + f.getName());
            }
            files.add(r);
        }
        return files;
    }

    public boolean deleteFile(String fileName) {
        return new File(OUTPUT_DIR + fileName).delete();
    }

    public File getFile(String fileName) {
        return new File(OUTPUT_DIR + fileName);
    }
}
