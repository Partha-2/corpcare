package com.corpcare.pdfsplit.model.response;

import lombok.Data;
import java.util.List;

@Data
public class CombinedResult {
    private List<SplitResult> splitResults;
    private List<AnalysisResult> imageResults;
}
