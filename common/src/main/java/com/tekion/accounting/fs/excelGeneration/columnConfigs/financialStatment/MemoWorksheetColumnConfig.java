package com.tekion.accounting.fs.excelGeneration.columnConfigs.financialStatment;


import com.tekion.accounting.fs.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder;
import com.tekion.accounting.fs.excelGeneration.enums.ColumnFreezeType;
import lombok.Getter;

import static com.tekion.accounting.fs.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder.*;


@Getter
public enum MemoWorksheetColumnConfig implements AccAbstractColumnConfig {

    FS_PAGE("fsPage", "FS Page","fsPage", STANDARD_STRING),
    FS_LINE("fsLine", "FS Line","fsLine", STANDARD_STRING_FOR_FS_LINE),
    MEMO_DESCRIPTION("description", "Memo Description","description", STANDARD_STRING),
    STATUS("status", "Status", "status", STANDARD_STRING),
    MTD_VALUE("mtdValue", "Value(MTD)", null, DECIMAL_2_PLACES_NUMBER),
    YTD_VALUE("ytdValue", "Value(YTD)", null, DECIMAL_2_PLACES_NUMBER),
    ;

    private final String beanKey;
    private final String columnDisplayName;
    private final ExcelCellFormattingHolder excelCellFormatting;
    private final String sortKeyMapping;
    private final String preferenceColumnKey;
    private final ColumnFreezeType columnFreezeType;

    MemoWorksheetColumnConfig(String beanKey, String columnDisplayName, String sortKeyMapping, ExcelCellFormattingHolder formattingHolder) {
        this.beanKey = beanKey;
        this.columnDisplayName = columnDisplayName;
        this.excelCellFormatting = formattingHolder;
        this.sortKeyMapping = sortKeyMapping;
        this.preferenceColumnKey = null;
        this.columnFreezeType = null;
    }

}
