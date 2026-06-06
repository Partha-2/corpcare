package com.corpcare.pdfsplit.controller;

import com.corpcare.pdfsplit.model.response.ApiResponse;
import com.corpcare.pdfsplit.model.response.CombinedResult;
import com.corpcare.pdfsplit.model.response.SplitResult;
import com.corpcare.pdfsplit.service.pdf.SmartPdfSplitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pdf")
public class PdfSplitController {

    private final SmartPdfSplitterService splitter;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<CombinedResult>> uploadPdf(
            @RequestParam("file") MultipartFile file) {
        try {
            CombinedResult result = splitter.process(file);
            return ResponseEntity.ok(
                    ApiResponse.success("PDF uploaded and split successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SplitResult>>> listFiles() {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success("Files fetched", splitter.getAllFiles()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }

    @GetMapping("/{fileName}/view")
    public ResponseEntity<?> viewFile(@PathVariable String fileName) {
        File file = splitter.getFile(fileName);
        if (file == null || !file.exists())
            return ResponseEntity.status(404)
                    .body(ApiResponse.failure("File not found: " + fileName));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/{fileName}/download")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
        File file = splitter.getFile(fileName);
        if (file == null || !file.exists())
            return ResponseEntity.status(404)
                    .body(ApiResponse.failure("File not found: " + fileName));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(file));
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<ApiResponse<String>> deleteFile(@PathVariable String fileName) {
        try {
            boolean deleted = splitter.deleteFile(fileName);
            if (!deleted)
                return ResponseEntity.status(404)
                        .body(ApiResponse.failure("File not found: " + fileName));
            return ResponseEntity.ok(
                    ApiResponse.success(fileName + " deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }
}
