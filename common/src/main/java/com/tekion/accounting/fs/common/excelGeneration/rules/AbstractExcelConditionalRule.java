package com.tekion.accounting.fs.common.excelGeneration.rules;

import com.tekion.accounting.fs.common.excelGeneration.columnConfigs.ExcelColumnConfigGeneratorService;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import com.tekion.core.utils.TCollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public abstract class AbstractExcelConditionalRule implements ExcelColumnConditioningRule {
    @Autowired
    private ExcelColumnConfigGeneratorService excelColumnConfigGeneratorService;

    @Override
    public List<ColumnConfig> applyRule(List<ColumnConfig> sourceColumnConfigs, ColumnConfig conditionedOnColumn, ColumnConfig columnOnWhichToApplyCondition) {
        return doApplyRule(sourceColumnConfigs,conditionedOnColumn,columnOnWhichToApplyCondition);
    }

    protected abstract List<ColumnConfig> doApplyRule(List<ColumnConfig> sourceColumnConfigs, ColumnConfig conditionedOnColumn, ColumnConfig columnOnWhichToApplyCondition) ;


    protected void doFixOrderOfColumnConfigs(List<ColumnConfig> columnConfigs){
        if(TCollectionUtils.isEmpty(columnConfigs)){
            return;
        }
        excelColumnConfigGeneratorService.fixOrderOfColumnsInGeneratedList(columnConfigs);
    }

}
