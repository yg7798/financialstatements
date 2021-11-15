package com.tekion.accounting.fs.common.excelGeneration.rules;

import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import com.tekion.core.utils.TCollectionUtils;

import java.util.List;
import java.util.Objects;


// simply appends column to right and returns column config.
public class AppendColumnToRightIfConditionalColumnExistRule extends AbstractExcelConditionalRule {
    private static AppendColumnToRightIfConditionalColumnExistRule singletonInstance;


    @Override
    protected List<ColumnConfig> doApplyRule(List<ColumnConfig> sourceColumnConfigs, ColumnConfig conditionedOnColumn, ColumnConfig columnOnWhichToApplyCondition) {
        if(TCollectionUtils.isEmpty(sourceColumnConfigs)){
            return sourceColumnConfigs;
        }

        for (int i = 0; i < sourceColumnConfigs.size(); i++) {
            ColumnConfig columnConfig = sourceColumnConfigs.get(i);
            if(conditionedOnColumn.getKey().equals(columnConfig.getKey())){
                sourceColumnConfigs.add(i+1,columnOnWhichToApplyCondition);
                break;
            }
        }
        return sourceColumnConfigs;
    }


    public static AppendColumnToRightIfConditionalColumnExistRule getInstanceOfExecutor(){
        if(Objects.isNull(singletonInstance)){
            singletonInstance = new AppendColumnToRightIfConditionalColumnExistRule();
            return singletonInstance;
        }
        return singletonInstance;
    }

    // singletons only
    private AppendColumnToRightIfConditionalColumnExistRule(){

    }
}
