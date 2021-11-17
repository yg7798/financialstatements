package com.tekion.accounting.fs.service.common.excelGeneration.helper;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder;
import com.tekion.accounting.fs.common.core.metadata.AccountingBeanMetaDataHolder;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.as.models.dto.TekTextSearchAndAggregationRequest;
import com.tekion.core.es.common.i.TekSort;
import com.tekion.core.es.common.impl.TekFilterRequest;
import com.tekion.core.es.common.impl.TekSearchRequest;
import com.tekion.core.excelGeneration.models.comparators.IColumnComparator;
import com.tekion.core.excelGeneration.models.enums.Comparator;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import com.tekion.core.excelGeneration.models.model.Sort;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ExcelGeneratorHelperUtil {

    public static Map<Comparator, IColumnComparator> mapComparatorsByDataType = Maps.newHashMap();

    public static TekTextSearchAndAggregationRequest getSearchRequestCopy(TekTextSearchAndAggregationRequest searchRequest) {
        List<TekFilterRequest> filters = (List<TekFilterRequest>) searchRequest.getFilters();
        if (Objects.isNull(filters)) {
            return null;
        }
        return JsonUtil.fromJson(JsonUtil.toJson(searchRequest), TekTextSearchAndAggregationRequest.class).orElse(null);
    }

    public static Sort removeAbsoluteAmountFromSearchRequestAndReturn(TekSearchRequest searchRequest, String keyToKeep){
        for (int i = 0; i < TCollectionUtils.nullSafeList(searchRequest.getSort()).size(); i++) {
            TekSort tekSort = searchRequest.getSort().get(i);
//            if(GLPosting.AMOUNT.equals(tekSort.getField()) && Objects.nonNull(tekSort.getTekScriptSort())){
//                searchRequest.getSort().remove(i);
//                return Sort.builder().key(keyToKeep).order(Sort.Order.valueOf(tekSort.getOrder().toString())).build();
//            }
        }
        return null;
    }

    public static IColumnComparator getComparatorBasedOnDataType(Comparator comparator) {
        if (mapComparatorsByDataType.isEmpty()) {
            registerComparators();
        }
        IColumnComparator columnComparator = mapComparatorsByDataType.get(comparator);
        if (Objects.isNull(columnComparator)) {
            return mapComparatorsByDataType.get(Comparator.STRING);
        }
        return columnComparator;
    }

    private static void registerComparators() {
        for (Comparator comparator : Comparator.values()) {
            try {
                mapComparatorsByDataType.put(comparator, comparator.getMappedComparatorClazz().newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("Error occurred while instantiating comparator ");
                throw new TBaseRuntimeException();
            }
        }
    }

    public static <T> void sortExcelRows(List<T> excelRows, List<TekSort> sortList, Class<T> clazz, Class<? extends AccAbstractColumnConfig> configClass) {
        AccountingBeanMetaDataHolder.registerFields(clazz);

        Map<String, AccAbstractColumnConfig> mapOfSortKeyByConfig = AccAbstractColumnConfig.getSortKeyToEnumMap(configClass.getName());
        if (TCollectionUtils.isEmpty(mapOfSortKeyByConfig)) {
            return;
        }
        Map<String, IColumnComparator> mapOfSortKeyByComparator = Maps.newHashMap();
        for (Map.Entry<String, AccAbstractColumnConfig> entry : mapOfSortKeyByConfig.entrySet()) {
            mapOfSortKeyByComparator.put(entry.getKey(), getComparatorBasedOnDataType(entry.getValue().getComparatorToUse()));
        }
        TCollectionUtils.nullSafeList(excelRows).sort((o1, o2) -> {
            int c = 0;
            for (TekSort sort : TCollectionUtils.nullSafeList(sortList)) {
                AccAbstractColumnConfig config = mapOfSortKeyByConfig.get(sort.getField());
                if (Objects.nonNull(config)) {
                    Method method = AccountingBeanMetaDataHolder.getMethod(clazz.getName(), config.getBeanKey());
                    try {
                        String value1 = (String) method.invoke(o1);
                        String value2 = (String) method.invoke(o2);
                        if (TConstants.NOT_AVAILABLE.equals(value1) || TConstants.NOT_AVAILABLE.equals(value2)) {
                            c = handleSortWithNACellValues(value1, value2);
                        } else {
                            c = mapOfSortKeyByComparator.get(sort.getField()).compare(value1, value2);
                        }
                        if (c != 0) {
                            if (TekSort.Order.DESC.equals(sort.getOrder())) {
                                return c * -1;
                            }
                            return c;
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new TBaseRuntimeException("Error occurred while sorting excel rows");
                    }
                }
            }
            return c;
        });
    }

    // N/A should be in the bottom when sorted in ASC order. Default sort in ASC order
    private static int handleSortWithNACellValues(String v1, String v2) {
        if (TConstants.NOT_AVAILABLE.equals(v1) && TConstants.NOT_AVAILABLE.equals(v2)) {
            return 0;
        } else if (TConstants.NOT_AVAILABLE.equals(v1)) {
            return 1;
        }
        return -1;
    }


    public static void putInColumnConfig(ExcelCellFormattingHolder cellFormattingHolder , ColumnConfig columnConfig){
        columnConfig.setCellType(cellFormattingHolder.getCellType());
        columnConfig.setDataType(cellFormattingHolder.getDataType());
        columnConfig.setHorizontalAlignment(cellFormattingHolder.getHorizontalAlignment());
        columnConfig.setPlaceHolderIfNull(cellFormattingHolder.getPlaceHolderIfNull());

        columnConfig.setFormattingOverride(cellFormattingHolder.getFormatOverride());

        columnConfig.setComparatorToUse(cellFormattingHolder.getComparatorToUse());
        columnConfig.setResolverToUse(cellFormattingHolder.getResolverToUse());
        columnConfig.setDateParseFormat(cellFormattingHolder.getDateParseFormat());


    }
}
