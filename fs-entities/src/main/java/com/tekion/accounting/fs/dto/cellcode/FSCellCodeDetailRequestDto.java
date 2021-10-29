package com.tekion.accounting.fs.dto.cellcode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FSCellCodeDetailRequestDto {
    private String fsCellCode;
    private String oemId;
    private Integer year;
}
