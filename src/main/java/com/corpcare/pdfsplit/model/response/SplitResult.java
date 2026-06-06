package com.corpcare.pdfsplit.model.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SplitResult {
    private String category;
    private String fileName;
    private int pageCount;
    private boolean found;
    private String downloadUrl;
    private String viewUrl;
    private String deleteUrl;
}
