package com.tekion.accounting.fs.dto.pclCodes;

import com.poiji.annotation.ExcelCell;
import com.tekion.core.exportable.lib.annotate.ExcelField;
import com.tekion.core.exportable.lib.annotate.Exportable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Exportable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PclUpdateExcelDto {
    @Builder.Default
    @ExcelCell(0)
    @ExcelField(name = "Group display code", order = 0)
    private String groupDisplayName = "";

    @Builder.Default
    @ExcelCell(1)
    @ExcelField(name = "Pcl code", order = 1)
    private String pclCode = "";
}
