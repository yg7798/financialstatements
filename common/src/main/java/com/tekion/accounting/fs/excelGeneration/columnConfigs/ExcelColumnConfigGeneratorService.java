package com.tekion.accounting.fs.excelGeneration.columnConfigs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.excelGeneration.columnConfigs.context.ColumnConfigComputeContext;
import com.tekion.accounting.fs.excelGeneration.columnConfigs.enums.PreferenceColumnMappingType;
import com.tekion.accounting.fs.excelGeneration.enums.ColumnFreezeType;
import com.tekion.accounting.fs.excelGeneration.enums.ExcelReportType;
import com.tekion.clients.preference.beans.ColumnPreference;
import com.tekion.clients.preference.beans.ListViewColumn;
import com.tekion.clients.preference.beans.ListViewPreference;
import com.tekion.clients.preference.beans.ListViewPreferenceResponse;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelColumnConfigGeneratorService {


    private final PreferenceClient preferenceClient;


    public void fixOrderOfColumnsInGeneratedList(List<ColumnConfig> columnConfigList) {
        int columnOrderToSet = 0;
        int invisibleColumnNumber = -1;
        for (ColumnConfig columnConfig : columnConfigList) {
            if(columnConfig.isExcludeFromExcel()){
                columnConfig.setOrder(invisibleColumnNumber--);
            }
            else{
                columnConfig.setOrder(columnOrderToSet++);
            }
        }
    }



    /**
     * This function has been written keep n mind that right now we only allow dynamic. (preference based columns for the plain view and not for the group by view.)
     * This implies that enablePreferenceBasedDynamic column WILL NEVER come as true for summary views of any excel. this will badly break otherwise.
     * might have to be adapted over time. Very messy function. Feel free to enhance :)
     * @param context
     * @return
     */
    public List<ColumnConfig> columnConfigGenerator( ColumnConfigComputeContext context){

        populateDefaultsInContext(context);

        List<ColumnConfig> finalColumnConfigList  = new ArrayList<>();

        if (context.isGeneratePreferenceBasedDynamicColumn()) {
            List<ColumnConfig> dynamicColumnsForReport = getDynamicColumnsForReport(context);
            dynamicColumnsForReport = applyConditionedColumnsIfAny(context,dynamicColumnsForReport);
            fixOrderOfColumnsInGeneratedList(dynamicColumnsForReport);
            return dynamicColumnsForReport;
        }

        List<AccAbstractColumnConfig> finalColumnList = getFinalColumnList(context);
        for (AccAbstractColumnConfig abstractColumnConfig : finalColumnList) {
            finalColumnConfigList.add(getSingleColumnConfig(abstractColumnConfig));
        }
        finalColumnConfigList = applyConditionedColumnsIfAny(context,finalColumnConfigList);
        fixOrderOfColumnsInGeneratedList(finalColumnConfigList);
        return finalColumnConfigList;
    }

    private List<ColumnConfig> applyConditionedColumnsIfAny(ColumnConfigComputeContext context, List<ColumnConfig> finalColumnConfigList) {
        finalColumnConfigList =doApplyColumnConfig( finalColumnConfigList,context.getGroupByColumnConfigs());
        finalColumnConfigList =doApplyColumnConfig( finalColumnConfigList,context.getBaseColumnConfigs());
        return finalColumnConfigList;
    }

    private List<ColumnConfig> doApplyColumnConfig(List<ColumnConfig> finalColumnConfigList,Class<? extends AccAbstractColumnConfig> columnConfigHolderClass) {
        boolean tryApplyGroupByColumnConfig = Objects.nonNull(columnConfigHolderClass);
        if(tryApplyGroupByColumnConfig){
            for (AccAbstractColumnConfig accAbstractColumnConfig : TCollectionUtils.nullSafeList(AccAbstractColumnConfig.getConditionedColumnsForConfig(columnConfigHolderClass.getName()))) {
                finalColumnConfigList = accAbstractColumnConfig.getConditioningRule()
                        .applyRule(finalColumnConfigList,accAbstractColumnConfig.getConditionedOnColumnConfig(),accAbstractColumnConfig.toColumnConfig());
            }
        }
        return finalColumnConfigList;
    }

    private List<AccAbstractColumnConfig> getFinalColumnList(ColumnConfigComputeContext context) {
        List<AccAbstractColumnConfig> finalColumnList = new ArrayList<>();

        if(context.isPlainView()){
            return TCollectionUtils.nullSafeList(AccAbstractColumnConfig.getEnumList(context.getBaseColumnConfigs().getName()));
        }

        if(context.isSummaryView()){
            return TCollectionUtils.nullSafeList(AccAbstractColumnConfig.getEnumList(context.getGroupByColumnConfigs().getName()));
        }

        AccAbstractColumnConfig groupByEnumVal = context.getGroupByEnumVal();
        boolean tryAndRemoveGroupByCol = Objects.nonNull(groupByEnumVal);

        boolean leftAlignGroupByColumns = context.isLeftAlignGroupByColumns();

        if(leftAlignGroupByColumns){
            finalColumnList.addAll(AccAbstractColumnConfig.getEnumList(context.getGroupByColumnConfigs().getName()));

            List<AccAbstractColumnConfig> enumList = AccAbstractColumnConfig.getEnumList(context.getBaseColumnConfigs().getName());

            for (AccAbstractColumnConfig accAbstractColumnConfig : enumList) {
                if(tryAndRemoveGroupByCol){
                    if(!groupByEnumVal.getBeanKey().equals(accAbstractColumnConfig.getBeanKey())){
                        finalColumnList.add(accAbstractColumnConfig);
                    }
                }
                else{
                    finalColumnList.add(accAbstractColumnConfig);
                }
            }
        }

        return finalColumnList;
    }

    // TODO: 21/10/20 move to a map

    private  AccAbstractColumnConfig getGroupingKeyEnumVal(Class<? extends AccAbstractColumnConfig> groupByColumnConfigs) {
        AccAbstractColumnConfig[] enumConstants = groupByColumnConfigs.getEnumConstants();
        if(Objects.isNull(enumConstants)){
            throw new TBaseRuntimeException("why is it not defined in an enum");
        }
        int counter =0;
        AccAbstractColumnConfig configToReturn = null;
        for (AccAbstractColumnConfig enumConstant : enumConstants) {
            AccAbstractColumnConfig linkedGroupingColumn = enumConstant.getLinkedGroupingColumn();
            if(Objects.nonNull(linkedGroupingColumn)){
                counter++;
                configToReturn = linkedGroupingColumn;
            }
        }
        if(counter>1){
            throw new TBaseRuntimeException("is grouping happening on multiple columns?");
        }

        return configToReturn;
    }
    private void appendFrozenColumnToList(List<ColumnConfig> columnConfigList, Map<ColumnFreezeType, List<AccAbstractColumnConfig>> columnFreezeTypeToColumnListMap, ColumnFreezeType freezeType) {
        List<AccAbstractColumnConfig> frozenColumnList = columnFreezeTypeToColumnListMap.get(freezeType);
        if (Objects.isNull(frozenColumnList)) {
            return;
        }
        for (AccAbstractColumnConfig accAbstractColumnConfig : frozenColumnList) {
            columnConfigList.add(getSingleColumnConfig(accAbstractColumnConfig));
        }
    }

    private ColumnConfig getSingleColumnConfig(AccAbstractColumnConfig accAbstractColumnConfig) {
        return accAbstractColumnConfig.toColumnConfig();
    }


    private List<ColumnConfig> getDynamicColumnsForReport(ColumnConfigComputeContext context) {

        String assetType;

        if(context.isOverridePreferenceAssetType()){
            assetType = context.getOverriddenPreferenceAssetType();
        }else{
            switch (context.getGeneratorVersion()) {
                case MULTI_SHEET:
                    assetType = context.getSheet().getColumnPreferenceReportKey();
                    break;
                default:
                    final ExcelReportType excelReportType = context.getExcelReportType();
                    assetType = excelReportType.getColumnPreferenceReportKey();
                    break;
            }
        }
        ListViewPreferenceResponse listViewPreferenceResponse = preferenceClient.fetchListViewPreferencesForUser(assetType);
        List<ListViewColumn> masterColumnList = listViewPreferenceResponse.getAllColumns().get(assetType);

        boolean useMasterColumnsList = false;
        if (Objects.isNull(masterColumnList) || TCollectionUtils.isEmpty(masterColumnList)) {
            //failFast approach
            throw new TBaseRuntimeException("trying to generate dynamic column for which columns have not been initialized. lol");
        }
        ListViewPreference listViewPreference = listViewPreferenceResponse.getPreferences().get(assetType);

        List<ColumnPreference> selectedColumns = null;

        if (Objects.isNull(listViewPreference)) {
            useMasterColumnsList = true;
        }
        else {
            selectedColumns = listViewPreference.getSelectedColumns();
            if (Objects.isNull(selectedColumns)) {
                useMasterColumnsList = true;
            }
        }

        if (useMasterColumnsList) {
            return computeUsingMasterList(masterColumnList, assetType, context);
        }
        Map<String, String> listViewIdToKeyMap = masterColumnList.stream().collect(Collectors.toMap(ListViewColumn::getId, ListViewColumn::getKey));
        return computeUsingColumnPreference(selectedColumns, assetType, context, listViewIdToKeyMap);
    }

    private List<ColumnConfig> computeUsingColumnPreference(List<ColumnPreference> selectedColumns, String assetType, ColumnConfigComputeContext context, Map<String, String> listViewIdToKeyMap) {
        List<ColumnConfig> columnConfigList = Lists.newArrayList();
        Map<String, AccColumnConfig> preferenceKeyToEnumMap = null;
        Map<String, List<AccColumnConfig>> preferenceKeyToEnumListMap= null;
        if(PreferenceColumnMappingType.ONE_TO_MANY.equals(context.getPreferenceColumnMappingType())){
            preferenceKeyToEnumListMap = getPreferenceKeyToAccColumnConfigList(context);
        }else{
            preferenceKeyToEnumMap = getPreferenceKeyToAccColumnConfig(context);
        }

        Map<ColumnFreezeType, List<AccAbstractColumnConfig>> columnFreezeTypeToColumnListMap = AccAbstractColumnConfig.getFrozenTypeToEnumListMap(context.getBaseColumnConfigs().getName());

        if(context.isDetailView() && context.isLeftAlignGroupByColumns()){
            appendGroupByColumnList(AccAbstractColumnConfig.getEnumList(context.getGroupByColumnConfigs().getName()),columnConfigList);
        }

        appendFrozenColumnToList(columnConfigList, columnFreezeTypeToColumnListMap, ColumnFreezeType.LEFT);


        AccAbstractColumnConfig columnConfigToSkip = context.getGroupByEnumVal();
        for (ColumnPreference listViewColumn : selectedColumns) {
            String keyForColumn = listViewIdToKeyMap.get(listViewColumn.getColumnId());
            if (TStringUtils.isBlank(keyForColumn)) {
                log.info("FATAL : EXCEL_COMPUTATION something went gravely wrong. A column has been registered in selected column which is missing in master columnList : for User : {} , forDealer : {} , forTenant : {} , {} report"
                        , UserContextProvider.getCurrentUserId(), UserContextProvider.getCurrentDealerId(), UserContextProvider.getCurrentTenantId(), assetType);
                continue;
            }
            if(PreferenceColumnMappingType.ONE_TO_MANY.equals(context.getPreferenceColumnMappingType())){
                List<AccColumnConfig> abstractColumnConfig = preferenceKeyToEnumListMap.get(keyForColumn);
                if (Objects.isNull(abstractColumnConfig)) {
                    continue;
                }
                addToList(columnConfigList,abstractColumnConfig,columnConfigToSkip);

            }
            else{
                AccColumnConfig abstractColumnConfig = preferenceKeyToEnumMap.get(keyForColumn);
                if (Objects.isNull(abstractColumnConfig)) {
                    continue;
                }
                addToList(columnConfigList,abstractColumnConfig,columnConfigToSkip);
            }
        }

        appendFrozenColumnToList(columnConfigList, columnFreezeTypeToColumnListMap, ColumnFreezeType.RIGHT);

        if(context.isDetailView() && !context.isLeftAlignGroupByColumns()){
            appendGroupByColumnList(AccAbstractColumnConfig.getEnumList(context.getGroupByColumnConfigs().getName()),columnConfigList);
        }


        fixOrderOfColumnsInGeneratedList(columnConfigList);

        return columnConfigList;
    }

    private Map<String, AccColumnConfig> getPreferenceKeyToAccColumnConfig(ColumnConfigComputeContext context) {
        if(Objects.nonNull(context.getListOfBaseColumnConfigsToBeUsed())){
            return convertColumnMapAgainstPrefKey(context.getListOfBaseColumnConfigsToBeUsed());
        }
        Map<String, AccAbstractColumnConfig> preferenceKeyToEnumMap = AccAbstractColumnConfig.getPreferenceKeyToEnumMap(context.getBaseColumnConfigs().getName());
        return convertColumnMapAgainstPrefKey(preferenceKeyToEnumMap);
    }

    private Map<String, AccColumnConfig> convertColumnMapAgainstPrefKey(List<ColumnConfig> listOfBaseColumnConfigsToBeUsed) {
        Map<String , AccColumnConfig> mapToReturn = Maps.newHashMap();
        for (ColumnConfig columnConfig : TCollectionUtils.nullSafeList(listOfBaseColumnConfigsToBeUsed)) {
            AccColumnConfig castedColumnConfig = (AccColumnConfig)columnConfig;
            mapToReturn.put(castedColumnConfig.getPreferenceColumnKey(),castedColumnConfig);
        }
        return mapToReturn;
    }

    private Map<String, AccColumnConfig> convertColumnMapAgainstPrefKey(Map<String, AccAbstractColumnConfig> preferenceKeyToEnumMap) {
        Map<String , AccColumnConfig> mapToReturn = Maps.newHashMap();
        for (Map.Entry<String, AccAbstractColumnConfig> stringAccAbstractColumnConfigEntry : TCollectionUtils.nullSafeMap(preferenceKeyToEnumMap).entrySet()) {

            mapToReturn.put(stringAccAbstractColumnConfigEntry.getKey(),stringAccAbstractColumnConfigEntry.getValue().toColumnConfig());
        }
        return mapToReturn;
    }

    private Map<String , List<AccColumnConfig>> convertToMapOfPrefKeyToColumnList(List<ColumnConfig> listOfBaseColumnConfigsToBeUsed) {
        Map<String , List<AccColumnConfig>> mapToReturn = Maps.newHashMap();
        for (ColumnConfig columnConfig : TCollectionUtils.nullSafeList(listOfBaseColumnConfigsToBeUsed)) {
            AccColumnConfig castedColumnConfig = (AccColumnConfig)columnConfig;

            mapToReturn.compute(castedColumnConfig.getPreferenceColumnKey(),(key,oldVal)->{
                oldVal = TCollectionUtils.nullSafeList(oldVal);
                oldVal.add(castedColumnConfig);
                return oldVal;
            }
            );
        }
        return mapToReturn;
    }

    private Map<String, List<AccColumnConfig>> getPreferenceKeyToAccColumnConfigList(ColumnConfigComputeContext context) {
        if(Objects.nonNull(context.getListOfBaseColumnConfigsToBeUsed())){
            return convertToMapOfPrefKeyToColumnList(context.getListOfBaseColumnConfigsToBeUsed());
        }
        Map<String, List<AccAbstractColumnConfig>> preferenceKeyToEnumListMap = AccAbstractColumnConfig.getPreferenceKeyToEnumListMap(context.getBaseColumnConfigs().getName());
        return convertToMapOfPrefKeyToColumnList(preferenceKeyToEnumListMap);

    }

    private Map<String, List<AccColumnConfig>> convertToMapOfPrefKeyToColumnList(Map<String, List<AccAbstractColumnConfig>> preferenceKeyToEnumListMap) {
        Map<String, List<AccColumnConfig>> mapToReturn = Maps.newHashMap();
        for (Map.Entry<String, List<AccAbstractColumnConfig>> stringAccAbstractColumnConfigEntry : TCollectionUtils.nullSafeMap(preferenceKeyToEnumListMap).entrySet()) {

            mapToReturn.compute(stringAccAbstractColumnConfigEntry.getKey(), (key, oldVal) -> {
                        oldVal = TCollectionUtils.nullSafeList(oldVal);
                        for (AccAbstractColumnConfig accAbstractColumnConfig : stringAccAbstractColumnConfigEntry.getValue()) {
                            oldVal.add(accAbstractColumnConfig.toColumnConfig());
                        }
                        return oldVal;
                    }
            );
        }
        return mapToReturn;
    }

    private List<ColumnConfig> computeUsingMasterList(List<ListViewColumn> masterColumnList, String assetType, ColumnConfigComputeContext context) {
        List<ColumnConfig> columnConfigList = Lists.newArrayList();

        if(context.isDetailView() && context.isLeftAlignGroupByColumns()){
            appendGroupByColumnList(AccAbstractColumnConfig.getEnumList(context.getGroupByColumnConfigs().getName()),columnConfigList);
        }


        Map<String, AccColumnConfig> preferenceKeyToEnumMap = null;
        Map<String, List<AccColumnConfig>> preferenceKeyToEnumListMap= null;
        if(PreferenceColumnMappingType.ONE_TO_MANY.equals(context.getPreferenceColumnMappingType())){
            preferenceKeyToEnumListMap = getPreferenceKeyToAccColumnConfigList(context);
        }else{
            preferenceKeyToEnumMap = getPreferenceKeyToAccColumnConfig(context);
        }
        Map<ColumnFreezeType, List<AccAbstractColumnConfig>> columnFreezeTypeToColumnListMap = AccAbstractColumnConfig.getFrozenTypeToEnumListMap(context.getBaseColumnConfigs().getName());

        List<ListViewColumn> sortedMasterColumnList = masterColumnList.stream().sorted(Comparator.comparingInt(ListViewColumn::getOrder)).collect(Collectors.toList());

        appendFrozenColumnToList(columnConfigList, columnFreezeTypeToColumnListMap, ColumnFreezeType.LEFT);

        //we still have to skip the column which was in groupBy but in subTable also :/
        AccAbstractColumnConfig columnConfigToSkip = context.getGroupByEnumVal();
        for (ListViewColumn listViewColumn : sortedMasterColumnList) {
            if(PreferenceColumnMappingType.ONE_TO_MANY.equals(context.getPreferenceColumnMappingType())){
                List<AccColumnConfig> abstractColumnConfig = preferenceKeyToEnumListMap.get(listViewColumn.getKey());
                if (Objects.isNull(abstractColumnConfig)) {
                    continue;
                }
                addToList(columnConfigList,abstractColumnConfig,columnConfigToSkip);

            }
            else{
                AccColumnConfig abstractColumnConfig = preferenceKeyToEnumMap.get(listViewColumn.getKey());
                if (Objects.isNull(abstractColumnConfig)) {
                    continue;
                }
                addToList(columnConfigList,abstractColumnConfig,columnConfigToSkip);
            }
        }

        appendFrozenColumnToList(columnConfigList, columnFreezeTypeToColumnListMap, ColumnFreezeType.RIGHT);

        if(context.isDetailView() && !context.isLeftAlignGroupByColumns()){
            appendGroupByColumnList(AccAbstractColumnConfig.getEnumList(context.getGroupByColumnConfigs().getName()),columnConfigList);
        }

        fixOrderOfColumnsInGeneratedList(columnConfigList);

        return columnConfigList;
    }

    private void addToList(List<ColumnConfig> columnConfigList, List<AccColumnConfig> abstractColumnConfig, AccAbstractColumnConfig columnConfigToSkip) {
        // if any of these preference keys belong to groupKey, we will skip all. this is an impossible scenario for now. and not even sure why this would ever be needed.
        // doing this arbitrary behaviour for now. when this is implemented for any actual report (i dont see ever being used), at that time logic can be handled
        if((Objects.nonNull(columnConfigToSkip) && TCollectionUtils.nullSafeList(abstractColumnConfig)
                .stream()
                .map(AccColumnConfig::getKey)
                .collect(Collectors.toSet())
                .contains(columnConfigToSkip.getBeanKey()))){ }
        else{
            for (AccColumnConfig accAbstractColumnConfig : abstractColumnConfig) {
                columnConfigList.add(accAbstractColumnConfig);
            }
        }
    }

    private void addToList(List<ColumnConfig> columnConfigList, AccColumnConfig abstractColumnConfig, AccAbstractColumnConfig columnConfigToSkip) {
        if((Objects.nonNull(columnConfigToSkip) && columnConfigToSkip.getBeanKey().equals(abstractColumnConfig.getKey()))){ }
        else{
            columnConfigList.add(abstractColumnConfig);
        }
    }

    private void appendGroupByColumnList(List<AccAbstractColumnConfig> enumList, List<ColumnConfig> columnConfigList) {
        if (Objects.isNull(enumList)) {
            return;
        }
        for (AccAbstractColumnConfig accAbstractColumnConfig : enumList) {
            columnConfigList.add(getSingleColumnConfig(accAbstractColumnConfig));
        }

    }



    private void populateDefaultsInContext(ColumnConfigComputeContext context) {
        String reportType = context.getReportType();

        switch (context.getGeneratorVersion()) {
            case MULTI_SHEET:
                context.setBaseColumnConfigs(context.getSheet().getBaseColumnConfigs());
                context.setPlainView(true);
                break;
            default:
                ExcelReportType excelReportTypeFromEnum = ExcelReportType.valueOf(reportType);
                context.setExcelReportType(excelReportTypeFromEnum);
                context.setBaseColumnConfigs(excelReportTypeFromEnum.getBaseColumnConfigs());
                if (Objects.isNull(context.getBaseColumnConfigs())) {
                    context.setSummaryView(true);
                }
                context.setGroupByColumnConfigs(excelReportTypeFromEnum.getGroupByColumnConfigs());
                ;
                if (Objects.isNull(context.getGroupByColumnConfigs())) {
                    context.setPlainView(true);
                }
                break;
        }

        if(context.isPlainView() && context.isSummaryView()){
            throw new TBaseRuntimeException("why are both enums not registered? where are the reports column configs defined?");
        }

        if(!context.isPlainView() && !context.isSummaryView()){
            context.setDetailView(true);
        }

        if(context.isDetailView()){
            context.setGroupByEnumVal(getGroupingKeyEnumVal(context.getGroupByColumnConfigs()));
        }
    }

    public String determineCorrespondingSortKeyToSend(ExcelReportType excelReportType, String fieldForWhichToFindMapping) {
        if(Objects.nonNull(excelReportType.getBaseColumnConfigs())){
            String beanKeyForGivenSortKeySentByUI = AccAbstractColumnConfig.getBeanKeyForGivenSortKeySentByUI(fieldForWhichToFindMapping, excelReportType.getBaseColumnConfigs().getName());
            if(Objects.nonNull(beanKeyForGivenSortKeySentByUI)){
                return beanKeyForGivenSortKeySentByUI;
            }
        }
        if(Objects.nonNull(excelReportType.getGroupByColumnConfigs())){
            String beanKeyForGivenSortKeySentByUI = AccAbstractColumnConfig.getBeanKeyForGivenSortKeySentByUI(fieldForWhichToFindMapping, excelReportType.getGroupByColumnConfigs().getName());
            if(Objects.nonNull(beanKeyForGivenSortKeySentByUI)){
                return beanKeyForGivenSortKeySentByUI;
            }

        }
        return null;
    }
}
