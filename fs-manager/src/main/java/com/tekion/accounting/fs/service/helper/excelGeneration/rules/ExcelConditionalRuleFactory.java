package com.tekion.accounting.fs.service.helper.excelGeneration.rules;

import com.tekion.accounting.fs.service.helper.excelGeneration.enums.ConditionalRule;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class ExcelConditionalRuleFactory {

    public static ExcelColumnConditioningRule getRuleExecutorInstance(ConditionalRule rule){

        if(Objects.isNull(rule)){
            throw new TBaseRuntimeException();
        }
        switch (rule){
            case APPEND_COLUMN_ON_RIGHT_OF_CONDTIONED_COLUMN:
                return AppendColumnToRightIfConditionalColumnExistRule.getInstanceOfExecutor();

        }
        return AppendColumnToRightIfConditionalColumnExistRule.getInstanceOfExecutor();
    }


}
