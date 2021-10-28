package com.tekion.accounting.fs.memo.dto;
import lombok.Data;

import java.util.List;

@Data
public class MemoBulkUpdateDto {
    List<MemoWorkSheetUpdateDto> memoWorksheets;
    String siteId;
}
