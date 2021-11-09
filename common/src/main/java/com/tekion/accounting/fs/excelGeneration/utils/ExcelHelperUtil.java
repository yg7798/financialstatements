package com.tekion.accounting.fs.excelGeneration.utils;

import com.google.common.collect.Lists;
import com.tekion.accounting.fs.core.minimisedResource.MinimizedResource;
import com.tekion.accounting.fs.core.minimisedResource.MinimizedResourceMetaData;
import com.tekion.accounting.fs.excelGeneration.columnConfigs.AccColumnConfig;
import com.tekion.accounting.fs.utils.JsonUtil;
import com.tekion.core.es.common.i.TekSort;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import com.tekion.core.excelGeneration.models.model.Sort;
import com.tekion.core.excelGeneration.models.model.v2.SingleRowData;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

;

@UtilityClass
public class ExcelHelperUtil {

//    public static void populateLookupAssetMapInContext(ExcelReportContext context, Set<String> userIdList, ResolveAssetUtils resolveAssetUtils) {
//        List<LookUpDetails> lookUpDetails = Lists.newArrayList();
//
//        for (String modifiedByUserId : TCollectionUtils.nullSafeCollection(userIdList)) {
//            lookUpDetails.add(LookUpDetails.builder().idToLookUp(modifiedByUserId).lookupAsset(LookupAsset.TENANT_USER_MINIMAL).build());
//        }
//
//        if(TCollectionUtils.isNotEmpty(lookUpDetails)){
//            populateResolvedUserMap(context, lookUpDetails, resolveAssetUtils);
//        }
//    }

    public static <T extends ColumnConfig> List<AccColumnConfig> createDeepCopies(List<T> columnConfigList){

        List<AccColumnConfig> returnList = Lists.newArrayList();
        for (T t : columnConfigList) {
            returnList.add(JsonUtil.fromJson(JsonUtil.toJson(t), AccColumnConfig.class).orElse(null));
        }
        return returnList;
    }

//    private static void populateResolvedUserMap(ExcelReportContext context, List<LookUpDetails> lookUpDetails, ResolveAssetUtils resolveAssetUtils) {
//        Map<LookupAsset, Map<String, JsonNode>> lookupResolvedAssetMap = resolveAssetUtils.bulkLookUpForIds(lookUpDetails);
//        for (Map.Entry<LookupAsset, Map<String, JsonNode>> lookupAssetMapEntry : lookupResolvedAssetMap.entrySet()) {
//            LookupAsset key = lookupAssetMapEntry.getKey();
//            for (Map.Entry<String, JsonNode> stringJsonNodeEntry : lookupAssetMapEntry.getValue().entrySet()) {
//                String idForLook = stringJsonNodeEntry.getKey();
//                context.getLookupAssetMap().compute(key, (key1, oldVal)->{
//                    oldVal = TCollectionUtils.nullSafeMap(oldVal);
//                    oldVal.compute(idForLook ,(key2,lookUpNode)-> stringJsonNodeEntry.getValue());
//                    return oldVal;
//                });
//            }
//        }
//    }

    public <T> List<SingleRowData> getSingleRowDataList(List<T> reportRows, MinimizedResourceMetaData minimizedResourceMetaData) {
        List<SingleRowData> singleRowDataList =new ArrayList<>();
        for (T row : reportRows) {
            MinimizedResource<T> minimizedResource  = new MinimizedResource<>();
            SingleRowData singleRowData = new SingleRowData();
            minimizedResource.setData(row);
            minimizedResource.setMinimizedResourceMetaData(minimizedResourceMetaData);
            minimizedResource.setMinimizeObject(true);
            singleRowData.setObject(minimizedResource);
            singleRowDataList.add(singleRowData);
        }
        return singleRowDataList;
    }


    public <T> List<SingleRowData> getSingleRowDataList(List<T> reportRows) {
        List<SingleRowData> singleRowDataList = new ArrayList<>();
        for(T row : reportRows)
        {
            singleRowDataList.add(getSingleRowData(row));
        }
        return singleRowDataList;
    }

    public <T> SingleRowData getSingleRowData(T reportRow) {
        SingleRowData singleRowData = new SingleRowData();
        singleRowData.setObject(reportRow);
        return singleRowData;
    }

    public static List<Sort> getSorts(final List<TekSort> tekSorts) {
        final List<Sort> sorts = new ArrayList<>(tekSorts.size());
        for (TekSort tekSort: tekSorts) {
            sorts.add(Sort.builder()
                    .key(tekSort.getField())
                    .order(Sort.Order.valueOf(tekSort.getOrder().name()))
                    .build());
        }
        return sorts;
    }
}
