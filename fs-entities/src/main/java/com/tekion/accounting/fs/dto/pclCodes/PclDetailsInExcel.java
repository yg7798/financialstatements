package com.tekion.accounting.fs.dto.pclCodes;

import com.poiji.annotation.ExcelCell;
import lombok.Data;


@Data
public class PclDetailsInExcel {
    @ExcelCell(0)
    private String OEM_ID;
    @ExcelCell(1)
    private String YEAR;
    @ExcelCell(2)
    private String COUNTRY;
    @ExcelCell(3)
    private String GROUP_DISPLAY_NAME;
    @ExcelCell(4)
    private String GROUP_CODE;
    @ExcelCell(5)
    private String CDK_PCL;
    @ExcelCell(6)
    private String DB_PCL;
    @ExcelCell(7)
    private String RR_PCL;
    @ExcelCell(8)
    private String DOMINION_PCL;
    @ExcelCell(9)
    private String QUORUM_PCL;
    @ExcelCell(10)
    private String AUTO_SOFT_PCL;
    @ExcelCell(11)
    private String AUTOMATE_PCL;
    @ExcelCell(12)
    private String PBS_PCL;
    @ExcelCell(13)
    private String DEALER_TRACK_PCL;
}
