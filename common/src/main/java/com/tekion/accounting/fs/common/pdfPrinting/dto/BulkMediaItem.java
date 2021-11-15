package com.tekion.accounting.fs.common.pdfPrinting.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkMediaItem {

    public static final int DEFAULT_PAGE_COUNT = -1;

    @NotNull private String mediaId;
    private Long mediaSize;
    @NotNull private String contentType;
    private String objectURL;
    private Long createdTime;
    private String originalFileName;
    private int pageCount = DEFAULT_PAGE_COUNT;


    public static MediaItem toMediaItem(BulkMediaItem bulkMediaItem){
        return MediaItem.builder().id(bulkMediaItem.mediaId).url(bulkMediaItem.objectURL)
            .contentType(bulkMediaItem.contentType).originalFileName(bulkMediaItem.originalFileName)
            .pageCount(bulkMediaItem.pageCount).build();
    }
}
