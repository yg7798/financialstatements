package com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.financialStatment;

import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ColumnFreezeType;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.Getter;

import java.util.Objects;

import static com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder.*;


@Getter
public enum OEMMappingColumnConfig implements AccAbstractColumnConfig {

    FRANCHISE("franchise", "Franchise", STANDARD_STRING, null),
    STATUS("status", "Status",STANDARD_STRING, null),
    GL_NUMBER("glAccountNumber", "GL Number",STRING_FORMAT_OVERRIDE_FOR_NUMBER, "accountNumber"),
    GL_NAME("glAccountName", "GL Name", STANDARD_STRING, "accountName"),
    ACCOUNT_STATUS("accountStatus", "Account Status", STANDARD_STRING, null),
    ACCOUNT_TYPE("accountType", "Account Type", STANDARD_STRING, "accountTypeId"),
    DEPARTMENT("department", "Department", STANDARD_STRING, null),
    YTD_BALANCE("ytdBalance", "Balance (YTD)", DECIMAL_2_PLACES_NUMBER, null),
    MTD_BALANCE("mtdBalance", "Balance (MTD)", DECIMAL_2_PLACES_NUMBER, null),
    YTD_COUNT("ytdCount", "Count (YTD)", INTEGER_NUMBER, null),
    MTD_COUNT("mtdCount", "Count (MTD)", INTEGER_NUMBER, null),
    GROUP_CODES("groupCodes", "Mapping", STANDARD_STRING, null),
        ;

    private final String beanKey;
    private final String columnDisplayName;
    private final ExcelCellFormattingHolder excelCellFormatting;
    private final String sortKeyMapping;
    private final String preferenceColumnKey;
    private final ColumnFreezeType columnFreezeType;

    OEMMappingColumnConfig(String beanKey, String columnDisplayName, ExcelCellFormattingHolder formattingHolder, String sortKeyMapping) {
        this.beanKey = beanKey;
        this.columnDisplayName = columnDisplayName;

        if(Objects.isNull(formattingHolder)){
            throw new TBaseRuntimeException();
        }

        this.excelCellFormatting = formattingHolder;
        this.sortKeyMapping = sortKeyMapping;
        this.preferenceColumnKey = null;
        this.columnFreezeType = null;
    }

}
