package com.corpcare.pdfanalyzer.exception;

import lombok.Getter;

@Getter
public class PdfSplitterException extends RuntimeException {
    private final int statusCode;

    public PdfSplitterException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
