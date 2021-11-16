package com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.enums;

import com.tekion.accounting.fs.common.TConstants;
import com.tekion.core.excelGeneration.models.enums.Comparator;
import com.tekion.core.excelGeneration.models.enums.FormatOverride;
import com.tekion.core.excelGeneration.models.enums.Resolver;
import lombok.Getter;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import static com.tekion.accounting.fs.common.TConstants.*;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT;
import static org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT;


@Getter
public enum ExcelCellFormattingHolder {
    STANDARD_STRING(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.STRING, Resolver.DEFAULT),
    STANDARD_STRING_FOR_FS_LINE(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT),
    STANDARD_STRING_RIGHT(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, RIGHT, "", FormatOverride.NONE, Comparator.STRING, Resolver.DEFAULT),
    STRING_FORMAT_OVERRIDE_FOR_NUMBER(EXCEL_DATA_TYPE_0, NUMERIC, RIGHT, "", FormatOverride.STRING, Comparator.STRING, Resolver.DEFAULT),
    STRING_FORMAT_OVERRIDE_FOR_NUMBER_LEFT(EXCEL_DATA_TYPE_0, NUMERIC, LEFT, "", FormatOverride.STRING, Comparator.STRING, Resolver.DEFAULT),
    STRING_FORMAT_OVERRIDE_FOR_NUMBER_LONG_COMPARATOR(EXCEL_DATA_TYPE_0, NUMERIC, RIGHT, "", FormatOverride.STRING, Comparator.LONG, Resolver.DEFAULT),
    STRING_FORMAT_OVERRIDE_FOR_NUMERIC_STRING_COMPARATOR(EXCEL_DATA_TYPE_0, NUMERIC, RIGHT, "", FormatOverride.STRING, Comparator.STRING_AND_NUMERIC_WITH_STRING_VALUE, Resolver.DEFAULT),
    STRING_FORMAT_OVERRIDE_FOR_NUMERIC_STRING_COMPARATOR_LEFT(EXCEL_DATA_TYPE_0, NUMERIC, LEFT, "", FormatOverride.STRING, Comparator.STRING_AND_NUMERIC_WITH_STRING_VALUE, Resolver.DEFAULT),
    INTEGER_NUMBER(EXCEL_DATA_TYPE_0, NUMERIC, RIGHT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT),
    DECIMAL_2_PLACES_NUMBER(EXCEL_DATA_TYPE_0_00, NUMERIC, RIGHT, "", FormatOverride.NONE, Comparator.BIG_DECIMAL, Resolver.DEFAULT),
    DECIMAL_2_PLACES_NUMBER_ABSOLUTE_SORT(EXCEL_DATA_TYPE_0_00, NUMERIC, RIGHT, "", FormatOverride.NONE, Comparator.ABSOLUTE_BIG_DECIMAL, Resolver.DEFAULT),

    //uses the default format in Resolver.DEFAULT_DATE mapped class,
    DATE_WITH_DEFAULT_FORMAT(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT_DATE,null),
    @Deprecated
    DATE_WITH_DD_MM_YY_HH_MM_AA_FORMAT(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "",FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT_DATE, TConstants.DATE_FORMAT_DD_MM_YY_HH_MM_AA),
    @Deprecated
    DATE_WITH_MM_DD_YYYY_FORMAT(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT_DATE, TConstants.MM_dd_yyyy),
    @Deprecated
    DATE_WITH_MM_DD_YY_FORMAT(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT_DATE, TConstants.MM_dd_yy),

    NEW_DATE_WITH_M_D_YY_FORMAT(EXCEL_DATA_TYPE_M_D_YY, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT, null),
    NEW_DATE_WITH_MM_DD_YY_FORMAT(EXCEL_DATA_TYPE_MM_DD_YY, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT, null),
    NEW_DATE_WITH_MM_DD_YYYY_FORMAT(EXCEL_DATA_TYPE_MM_DD_YYYY, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT, null),
    TIME_WITH_hh_mm_aa_format(EXCEL_DATA_TYPE_h_m_aa, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT_DATE, TConstants.HH_mm_ss),
    TIME_WITH_hh_mm_ss_aa_format(EXCEL_DATA_TYPE_h_m_ss_aa, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.LONG, Resolver.DEFAULT_DATE, TConstants.HH_mm_ss),
    PERCENTAGE_2_PLACES(EXCEL_DATA_TYPE_PERCENT, NUMERIC, RIGHT, "", FormatOverride.NONE, Comparator.BIG_DECIMAL, Resolver.DEFAULT),



    STANDARD_STRING_WITH_DIFF_COMPARATOR(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.STRING_KEEP_BLANK_VALUES_AT_LAST_IN_ASC_ORDER, Resolver.DEFAULT),
    STRING_NUMERIC(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.STRING_AND_NUMERIC, Resolver.DEFAULT),
    STRING_NUMERIC_WITH_DIFF_COMPARATOR(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.STRING_AND_NUMERIC_KEEP_BLANK_VALUES_AT_LAST_IN_ASC_ORDER, Resolver.DEFAULT),
    // string cellType with numerics also being compared properly
    STRING_NUMERIC_WITH_DIFF_COMPARATOR_WITH_PREFIX_VALUE(EXCEL_DATA_TYPE_GENERAL, CellType.STRING, LEFT, "", FormatOverride.NONE, Comparator.STRING_AND_NUMERIC_WITH_STRING_VALUE, Resolver.DEFAULT)
    ;
    private final String dataType;
    private final CellType cellType;
    private final HorizontalAlignment horizontalAlignment;
    private final String placeHolderIfNull;
    private final FormatOverride formatOverride;
    private final Comparator comparatorToUse;
    private final Resolver resolverToUse;
    private final String dateParseFormat;

    private ExcelCellFormattingHolder(String dataType, CellType cellType, HorizontalAlignment horizontalAlignment, String placeHolderIfNull, FormatOverride formatOverride, Comparator comparatorToUse, Resolver resolverToUse, String dateParseFormat) {
        this.dataType = dataType;
        this.cellType = cellType;
        this.horizontalAlignment = horizontalAlignment;
        this.placeHolderIfNull = placeHolderIfNull;
        this.formatOverride = formatOverride;
        this.comparatorToUse = comparatorToUse;
        this.resolverToUse = resolverToUse;
        this.dateParseFormat = dateParseFormat;
    }

    private ExcelCellFormattingHolder(String dataType, CellType cellType, HorizontalAlignment horizontalAlignment, String placeHolderIfNull, FormatOverride formatOverride, Comparator comparatorToUse, Resolver resolverToUse) {
        this.dataType = dataType;
        this.cellType = cellType;
        this.horizontalAlignment = horizontalAlignment;
        this.placeHolderIfNull = placeHolderIfNull;
        this.formatOverride = formatOverride;
        this.comparatorToUse = comparatorToUse;
        this.resolverToUse = resolverToUse;
        this.dateParseFormat = null;
    }
}
