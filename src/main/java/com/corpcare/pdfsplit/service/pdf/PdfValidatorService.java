package com.corpcare.pdfsplit.service.pdf;

import com.corpcare.pdfsplit.exception.PdfSplitterException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfValidatorService {

    public PDDocument validateAndOpen(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty())
            throw new PdfSplitterException("Please upload a PDF file", 400);

        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".pdf"))
            throw new PdfSplitterException("Only PDF files are allowed", 400);

        if (file.getSize() > 50L * 1024 * 1024)
            throw new PdfSplitterException("File too large. Max 50MB", 400);

        PDDocument doc;
        try {
            doc = Loader.loadPDF(file.getBytes());
        } catch (IOException e) {
            throw new PdfSplitterException("PDF is corrupted: " + e.getMessage(), 400);
        }

        if (doc.getNumberOfPages() == 0) {
            doc.close();
            throw new PdfSplitterException("PDF has no pages", 400);
        }

        if (doc.isEncrypted()) {
            doc.close();
            throw new PdfSplitterException("PDF is password protected", 400);
        }

        return doc;
    }
}
