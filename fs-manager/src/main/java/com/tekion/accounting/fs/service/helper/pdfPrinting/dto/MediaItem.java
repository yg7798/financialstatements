package com.tekion.accounting.fs.service.helper.pdfPrinting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaItem {
  @NotNull private String id;
  @NotNull private String url;
  private String previewUrl;
  @NotNull private String contentType;
  /** {@link MediaType} */
  private String type;
  private int pageCount = -1;

  private String originalFileName;
  private String label;
  private String category;
  private boolean dseMedia;
}
