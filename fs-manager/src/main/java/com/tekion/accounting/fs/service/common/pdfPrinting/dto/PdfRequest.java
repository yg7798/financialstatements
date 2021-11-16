package com.tekion.accounting.fs.service.common.pdfPrinting.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class PdfRequest  {
  public static final String TARGET_URL_KEY = "targetUrl";

  private String id;
  private String assetId;
  private String assetType;
  @Builder.Default private String scaleFactor = PdfScaleFactor.ONE.name();
  /** {@link Orientation} */
  @Builder.Default private String orientation = Orientation.PORTRAIT.name();

  /** {@link CopyType} */
  @Builder.Default private String copyType = CopyType.ACCOUNTING.name ();

  /** {@link PdfDocumentType} */
  @Builder.Default private String documentType = "Check";

  private List<MediaItem> mediaItems;

  @Transient
  private String callbackUrl;

  private String targetUrl;

  private String status;


  private Map<String, Object> extras;

}
