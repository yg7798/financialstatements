package com.tekion.accounting.fs.service.common.pdfPrinting.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BulkMediaItemsDto {
    private String assetId;
    @NotEmpty
    @Valid
    private List<BulkMediaItem> mediaItems;
}
