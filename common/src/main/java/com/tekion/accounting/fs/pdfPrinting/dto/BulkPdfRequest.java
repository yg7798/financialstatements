package com.tekion.accounting.fs.pdfPrinting.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkPdfRequest {

    public static String PAYMENT_ADVICE_CHECK = "paymentAdviceCheck";
    public static String PAYROLL_ASSETS = "payrollAssets";

    private String id;
    private List<String> assetIds;
    private boolean shouldShowPostAhead;
    private String assetType;
    @JsonProperty("isCentralised")
    private boolean isCentralised;
    @Builder.Default private String scaleFactor = PdfScaleFactor.ONE.name();

    /** {@link Orientation} */
    @Builder.Default private String orientation = Orientation.PORTRAIT.name();

    /** {@link PdfDocumentType} */
    @Builder.Default private String documentType = "Check";

    private List<MediaItem> mediaItems;

    @Transient
    private String callbackUrl;

    private String status;

    private Map<String, Object> extras;
    private boolean respondWithMediaUrls;
    private String errorCallbackUrl;
    private String errorAssetId;
}
