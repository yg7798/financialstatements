package com.tekion.accounting.fs.common.dpProvider.enums;

import com.tekion.accounting.fs.common.TConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum  RegisteredDp {

    TB_CALCULATOR_VERSION_FOR_OEM_FS("TB_CALCULATOR_VERSION_FOR_OEM_FS",DpSupportedClass.STRING, DpLevel.GLOBAL),
    M13_CLOSE_FEATURE_ENABLED("M13_CLOSE_FEATURE_ENABLED",DpSupportedClass.BOOLEAN, DpLevel.GLOBAL),
    M13_OPEN_FEATURE_ENABLED("M13_OPEN_FEATURE_ENABLED",DpSupportedClass.STRING, DpLevel.GLOBAL),
    RETRY_CONFIG("RETRY_CONFIG", DpSupportedClass.STRING, DpLevel.GLOBAL),
    USE_ID_SORT_IN_CONTROL_BOOK_FETCH("USE_ID_SORT_IN_CONTROL_BOOK_FETCH",DpSupportedClass.STRING, DpLevel.GLOBAL),
    SLEEP_TIME_IN_SYNC_MILLIS("SLEEP_TIME_IN_SYNC_MILLIS", DpSupportedClass.LONG, DpLevel.GLOBAL),
    MAX_MONTHS_OF_CONTROL_BOOK_HISTORIES_ALLOWED("MAX_MONTHS_OF_CONTROL_BOOK_HISTORIES_ALLOWED", DpSupportedClass.LONG, DpLevel.GLOBAL),
    MAX_CONTROL_BOOK_FETCH_ALLOWED("MAX_CONTROL_BOOK_FETCH_ALLOWED",DpSupportedClass.INTEGER,DpLevel.GLOBAL),
    MONTH_LEDGER_BASED_TB_CALCULATION("MONTH_LEDGER_BASED_TB_CALCULATION", DpSupportedClass.BOOLEAN, DpLevel.GLOBAL),
    EXCEL_GENERATION_LAMBDA_API_GATEWAY_WARM_INSTANCE_COUNT("EXCEL_GENERATION_LAMBDA_API_GATEWAY_WARM_INSTANCE_COUNT", DpSupportedClass.LONG, DpLevel.GLOBAL),
    EXCEL_GENERATION_LAMBDA_API_GATEWAY_COLD_START_ALLOWANCE("EXCEL_GENERATION_LAMBDA_API_GATEWAY_COLD_START_ALLOWANCE", DpSupportedClass.LONG, DpLevel.GLOBAL);

    private final String dpName;
    private final DpSupportedClass dpSupportedClass;
    private final DpLevel defaultDpLevel;
    private final String moduleName;

    private RegisteredDp(String dpName, DpSupportedClass dpSupportedClass) {
        this.dpName = dpName;
        this.dpSupportedClass = dpSupportedClass;
        this.moduleName = TConstants.ACCOUNTING_MODULE;
        this.defaultDpLevel = null;
    }
    private RegisteredDp(String dpName, DpSupportedClass dpSupportedClass, DpLevel dpLevel) {
        this.dpName = dpName;
        this.dpSupportedClass = dpSupportedClass;
        this.moduleName = TConstants.ACCOUNTING_MODULE;
        this.defaultDpLevel  = dpLevel;
    }


    @Getter
    @AllArgsConstructor
    public enum DpSupportedClass {

        BOOLEAN(Boolean.class),
        STRING(String.class),
        INTEGER(Integer.class),
        LONG(Long.class)
        ;

        private Class<?> clazz;
    }
}
