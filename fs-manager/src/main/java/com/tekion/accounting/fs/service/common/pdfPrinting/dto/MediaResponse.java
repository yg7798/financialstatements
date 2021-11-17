package com.tekion.accounting.fs.service.common.pdfPrinting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaResponse {

    private String mediaId;

    private Map<String, String> responseMap;

}
