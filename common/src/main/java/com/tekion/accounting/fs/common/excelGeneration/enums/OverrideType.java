package com.tekion.accounting.fs.common.excelGeneration.enums;

import com.tekion.accounting.fs.common.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum  OverrideType {
    NUMBER(ExcelCellFormattingHolder.INTEGER_NUMBER),
    STRING(ExcelCellFormattingHolder.STRING_FORMAT_OVERRIDE_FOR_NUMBER);

    private final ExcelCellFormattingHolder cellFormattingHolder;
}
