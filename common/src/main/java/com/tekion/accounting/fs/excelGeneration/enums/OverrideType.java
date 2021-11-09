package com.tekion.accounting.fs.excelGeneration.enums;

import com.tekion.accounting.fs.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.tekion.accounting.fs.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder.STRING_FORMAT_OVERRIDE_FOR_NUMBER;


@RequiredArgsConstructor
@Getter
public enum  OverrideType {
    NUMBER(ExcelCellFormattingHolder.INTEGER_NUMBER),
    STRING(STRING_FORMAT_OVERRIDE_FOR_NUMBER);

    private final ExcelCellFormattingHolder cellFormattingHolder;
}
