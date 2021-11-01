package com.tekion.accounting.fs.pdfPrinting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaResponseEntity {
 private String status;
 private List<MediaResponse> data;
}

