package com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs;

import com.google.common.collect.Maps;
import com.tekion.accounting.commons.utils.LocaleUtils;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ColumnFreezeType;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ConditionalRule;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelFieldIdentifier;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.SupportedFormatOverrideIdentifiers;
import com.tekion.accounting.fs.service.common.excelGeneration.rules.ExcelColumnConditioningRule;
import com.tekion.accounting.fs.service.common.excelGeneration.rules.ExcelConditionalRuleFactory;
import com.tekion.core.excelGeneration.helpers.models.AbstractColumnConfig;
import com.tekion.core.excelGeneration.models.enums.Comparator;
import com.tekion.core.excelGeneration.models.enums.FormatOverride;
import com.tekion.core.excelGeneration.models.enums.Resolver;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TStringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public interface AccAbstractColumnConfig extends AbstractColumnConfig {

    String getMultilingualKey();
    Map<String,Map<String, AccAbstractColumnConfig>> configClassNameToSortKeyToEnumMap = Maps.newHashMap();
    // if 2 columns have to be driven by 1 sort key, use this method
    Map<String,Map<String, List<AccAbstractColumnConfig>>> configClassNameToPreferenceKeyToEnumListMap = Maps.newHashMap();

    Map<String, List<AccAbstractColumnConfig>> configClassNameToEnumList = Maps.newHashMap();
    Map<String,Map<String, AccAbstractColumnConfig>> configClassNameToPreferenceKeyToEnumMap = Maps.newHashMap();
    Map<String, Map<ColumnFreezeType,List<AccAbstractColumnConfig>>> configClassNameToFrozenTypeToEnumListMap = Maps.newHashMap();
    Map<String, List<AccAbstractColumnConfig>> configClassNameToConditionedColumnsMap = Maps.newHashMap();

    /**
     * This will be later used to compare and resolve with preferenceColumnAndGenerateColumnConfigsBasedOnPreferenceColumn
     *
     * @return
     */

    default String getPreferenceColumnKey() {
        return null;
    }

    default SupportedFormatOverrideIdentifiers getSupportedFormatOverrideIdentifiers() {
        return null;
    }

    default ExcelFieldIdentifier getExcelFieldIdentifier() {
        return null;
    }

    //making this concrete class in case of complex rules in future
    default AccColumnConfig getConditionedOnColumnConfig(){
        return null;
    }


    // just putting this rule by default because 99% cases will be satisfiedByThis.
    // dont want to keep default null otherwise will always have to changes enums to add a single rule
    default ExcelColumnConditioningRule getConditioningRule(){
        return ExcelConditionalRuleFactory.getRuleExecutorInstance(ConditionalRule.APPEND_COLUMN_ON_RIGHT_OF_CONDTIONED_COLUMN);
    }


    default ColumnFreezeType getColumnFreezeType( ){
        return null;
    }
    default ExcelCellFormattingHolder getExcelCellFormatting( ){
        return null;
    }
    /**
     * for example UI sending sortKey as scheduledTime maps to accountingDate in our column config.
     *
     * @return
     */
    String getSortKeyMapping(); //scheduledTime  //journalReportRow // accountingDate

    // this is the default definition of considering a column conditioned. can be changed if needed but dont really forsee a usecase
    default boolean isColumnConditioned(){
        return Objects.nonNull(this.getConditionedOnColumnConfig());
    }

    default AccAbstractColumnConfig getLinkedGroupingColumn(){
        return null;
    }

    /**
     * gives the corresponding beanKey for the sortKey that it has to map with
     * ex : { com.tekion.as.service.glAccount.beans.JournalReportRow} has a field accountingDate which maps from
     * { com.tekion.as.service.transaction.beans.GLPosting} scheduledTime
     * so if UI will send to sort on scheduledTime. This method will return accountingDate ( { com.tekion.as.service.glAccount.beans.JournalReportRow} field of
     * the bean which is actually returned to ExcelClient )
     *
     * @param requestSortKey
     * @return if not found. returns null. This has been done so mistakes are caught early as the clientValidation will reject the call.
     */
    static String getBeanKeyForGivenSortKeySentByUI(String requestSortKey, String configClassName) {
        ExcelColumnConfigGeneratorUtil.registerClassInSortKeyMap(configClassName);
        return Objects.nonNull(configClassNameToSortKeyToEnumMap.get(configClassName)) &&
                Objects.nonNull(configClassNameToSortKeyToEnumMap.get(configClassName).get(requestSortKey))
                ?  configClassNameToSortKeyToEnumMap.get(configClassName).get(requestSortKey).getBeanKey() : null ;
    }

    // returns without conditionedColumns
    static Map<String, AccAbstractColumnConfig> getSortKeyToEnumMap( String configClassName) {
        ExcelColumnConfigGeneratorUtil.registerClassInSortKeyMap(configClassName);
        return configClassNameToSortKeyToEnumMap.get(configClassName);
    }

    static List<AccAbstractColumnConfig> getConditionedColumnsForConfig( String configClassName) {
        ExcelColumnConfigGeneratorUtil.registerClassInConditionedColumnsMap(configClassName);
        return configClassNameToConditionedColumnsMap.get(configClassName);
    }

    // returns without conditionedColumns
    static Map<String, AccAbstractColumnConfig> getPreferenceKeyToEnumMap( String configClassName) {
        ExcelColumnConfigGeneratorUtil.registerClassInPreferenceKeyMap(configClassName);
        return configClassNameToPreferenceKeyToEnumMap.get(configClassName);
    }
    // returns without conditionedColumns
    static Map<String, List<AccAbstractColumnConfig>> getPreferenceKeyToEnumListMap( String configClassName) {
        ExcelColumnConfigGeneratorUtil.registerClassInPreferenceKeyToEnumListMap(configClassName);
        return configClassNameToPreferenceKeyToEnumListMap.get(configClassName);
    }

    // returns without conditionedColumns
    static List<AccAbstractColumnConfig> getEnumList( String configClassName) {
        ExcelColumnConfigGeneratorUtil.registerClassForEnumList(configClassName);
        return configClassNameToEnumList.get(configClassName);
    }

    // returns without conditionedColumns
    static Map<ColumnFreezeType, List<AccAbstractColumnConfig>> getFrozenTypeToEnumListMap( String configClassName) {
        ExcelColumnConfigGeneratorUtil.registerClassForColumnFreezeMapping(configClassName);
        return configClassNameToFrozenTypeToEnumListMap.get(configClassName);
    }

    default AccColumnConfig toColumnConfig(){
        doValidateColumnConfig(this);
        AccColumnConfig columnConfig;
        columnConfig = new AccColumnConfig();
        columnConfig.setFormatOverrideIdentifier(getSupportedFormatOverrideIdentifiers());
        columnConfig.setExcelFieldIdentifier(getExcelFieldIdentifier());
        columnConfig.setColumnName(LocaleUtils.translateLabel(this.getMultilingualKey()));
        columnConfig.setKey(getBeanKey());
        columnConfig.setPreferenceColumnKey(getPreferenceColumnKey());
        columnConfig.setSortLookupKey(getSortLookupKey());
        columnConfig.setBold(isBold());
        columnConfig.setItalic(isItalic());
        columnConfig.setUnderline(isUnderline());
        columnConfig.setCellBackgroundColor(getCellBackgroundColor());
        columnConfig.setExcludeFromExcel(isExcludeFromExcel());
        columnConfig.setFillPatternType(getFillPatternType());
        columnConfig.setFontColor(getFontColor());
        columnConfig.setFontSizeInPoints(getFontSizeInPoints());
        columnConfig.setFontName(getFontName());
        if(Objects.isNull(getExcelCellFormatting())) {
            columnConfig.setHorizontalAlignment(getHorizontalAlignment());
            columnConfig.setDataType(getDataType());
            columnConfig.setOrder(getDefaultOrder());
            columnConfig.setCellType(getCellType());
            columnConfig.setPlaceHolderIfNull(getPlaceHolderIfNull());
            columnConfig.setComparatorToUse(getComparatorToUse());
            columnConfig.setResolverToUse(getResolverToUse());
            columnConfig.setFormattingOverride(getFormatOverride());
            columnConfig.setDateParseFormat(getDateParseFormat());
        }else{
            ExcelCellFormattingHolder excelCellFormatting = getExcelCellFormatting();
            applyFormattingOnColumnConfigAndSetOrderTo0(excelCellFormatting,columnConfig);
        }
        if(!Objects.isNull(getComparatorToUse())){
            columnConfig.setComparatorToUse(getComparatorToUse());
        }

        columnConfig.setFormula(getFormula());
        return columnConfig;
    }

    static void doValidateColumnConfig(AccAbstractColumnConfig accAbstractColumnConfig){
        // a column cannot be conditioned on some other column while also being mapped by preference no such use case for now
        if(TStringUtils.isNotBlank(accAbstractColumnConfig.getPreferenceColumnKey()) && Objects.nonNull(accAbstractColumnConfig.getConditionedOnColumnConfig())){
            throw new TBaseRuntimeException();
        }
    }

    default boolean isOverrideSupported(){
        return Objects.nonNull(getSupportedFormatOverrideIdentifiers());
    }

    static void applyFormattingOnColumnConfigAndSetOrderTo0(ExcelCellFormattingHolder excelCellFormatting, ColumnConfig columnConfig){
        columnConfig.setOrder(0);
        applyFormattingOnColumnConfig(excelCellFormatting,columnConfig);
    }

    static void applyFormattingOnColumnConfig(ExcelCellFormattingHolder excelCellFormatting, ColumnConfig columnConfig){
        columnConfig.setHorizontalAlignment(excelCellFormatting.getHorizontalAlignment());
        columnConfig.setDataType(excelCellFormatting.getDataType());
        columnConfig.setCellType(excelCellFormatting.getCellType());
        columnConfig.setPlaceHolderIfNull(excelCellFormatting.getPlaceHolderIfNull());
        columnConfig.setComparatorToUse(excelCellFormatting.getComparatorToUse());
        columnConfig.setResolverToUse(excelCellFormatting.getResolverToUse());
        columnConfig.setFormattingOverride(excelCellFormatting.getFormatOverride());
        columnConfig.setDateParseFormat(excelCellFormatting.getDateParseFormat());
    }





    //these have been put in place so only need to override where it is actually needed to be defined.
    default CellType getCellType(){
        return null;
    }
    default int getDefaultOrder(){
        return 0;
    }
    default String getDataType(){
        return null;
    }
    default HorizontalAlignment getHorizontalAlignment(){
        return null;
    }
    default FormatOverride getFormatOverride(){
        return null;
    }
    default String getPlaceHolderIfNull(){
        return null;
    }
    default Comparator getComparatorToUse(){
        return null;
    }
    default Resolver getResolverToUse(){
        return null;
    }
    default String getDateParseFormat(){
        return null;
    }
    default String getSortLookupKey() { return null; }
    default boolean isBold() { return false; }
    default boolean isItalic() { return false; }
    default boolean isUnderline() { return false; }
}
