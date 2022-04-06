package com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.financialStatment;


import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ColumnFreezeType;
import lombok.Getter;

import static com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder.*;


@Getter
public enum MemoWorksheetColumnConfig implements AccAbstractColumnConfig {

    FS_PAGE("fsPage", "FS Page","fsPage", STANDARD_STRING, "memo.fsPage"),
    FS_LINE("fsLine", "FS Line","fsLine", STANDARD_STRING_FOR_FS_LINE, "memo.fsLine"),
    MEMO_DESCRIPTION("description", "Memo Description","description", STANDARD_STRING, "memo.description"),
    STATUS("status", "Status", "status", STANDARD_STRING, "memo.status"),
    MTD_VALUE("mtdValue", "Value(MTD)", null, DECIMAL_2_PLACES_NUMBER, "memo.mtdValue"),
    YTD_VALUE("ytdValue", "Value(YTD)", null, DECIMAL_2_PLACES_NUMBER, "memo.ytdValue"),
    ;

    private final String beanKey;
    private final String columnDisplayName;
    private final ExcelCellFormattingHolder excelCellFormatting;
    private final String sortKeyMapping;
    private final String preferenceColumnKey;
    private final ColumnFreezeType columnFreezeType;
    private final String multilingualKey;

    MemoWorksheetColumnConfig(String beanKey, String columnDisplayName, String sortKeyMapping, ExcelCellFormattingHolder formattingHolder, String multilingualKey) {
        this.beanKey = beanKey;
        this.columnDisplayName = columnDisplayName;
        this.excelCellFormatting = formattingHolder;
        this.sortKeyMapping = sortKeyMapping;
        this.preferenceColumnKey = null;
        this.columnFreezeType = null;
        this.multilingualKey = multilingualKey;
    }

}
