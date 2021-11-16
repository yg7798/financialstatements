package com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExcelColumnConfigConstants {

    //UI defined def
    public static final String GROUP_BY_KEY = "byGroupKey";
    public static final String GROUP_BY_AMOUNT = "byGroupAmount";
    public static final String GROUP_BY_GROSS_PROFIT = "byGroupGrossProfit";
    public static final String GROUP_BY_GLACCOUNT = "byGlAcctId";
    public static final String GROUP_BY_JOURNAL = "byJourId";
    public static final String GROUP_BY_REF_TEXT = "byRefTextId";
    public static final String GROUP_BY_GL_ACCOUNT_TYPE = "byGroupGlAccountType";

    //preferenceAssetTypeMapping
    public static final String INQUIRY_TOOL_ASSET_TYPE = "INQUIRY_TOOL";
    public static final String JOURNAL_REPORT_ASSET_TYPE = "JOURNAL_REPORT";
    public static final String BANK_REC_RECONCILED_POSTINGS_ASSET_TYPE = "BANK_REC_RECONCILED_POSTINGS";
}
