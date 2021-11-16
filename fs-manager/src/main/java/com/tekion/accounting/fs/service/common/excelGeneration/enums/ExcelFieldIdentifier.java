package com.tekion.accounting.fs.service.common.excelGeneration.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExcelFieldIdentifier {
    STATE_IDENTIFIER("State");

    private final String key;
}
