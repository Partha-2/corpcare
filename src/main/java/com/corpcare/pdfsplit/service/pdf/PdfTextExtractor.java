package com.corpcare.pdfsplit.service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfTextExtractor {

    private final PDFTextStripper stripper;

    public PdfTextExtractor() throws IOException {
        stripper = new PDFTextStripper();
    }

    public String extractPage(PDDocument doc, int pageIndex) throws IOException {
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        return stripper.getText(doc).toLowerCase().trim();
    }

    public List<String> extractAll(PDDocument doc) throws IOException {
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            texts.add(extractPage(doc, i));
        }
        return texts;
    }
}
