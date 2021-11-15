package com.tekion.accounting.fs.service.helper.excelGeneration.rules;


import com.tekion.core.excelGeneration.models.model.ColumnConfig;

import java.util.List;

/**
 *  applies the rule on the passed column config list.
 *  without this we will never have the ability to modify columns dynamically per dealer.
 *  we can write a factory which selects the correct rule and uses based on DP
 *
 *  potential case they want accountingTime on left of accountingDate. i can see someone asking for this. or some more complicated request
 *
 *  This has been exposed so we can support very complex rules in future
 */
public interface ExcelColumnConditioningRule {

    /**
     * example usage
     * say you want to append accountingDatesTimeStamp column next to accountingDate column on the right
     * and accountingDatesTimeStamp doesnt exist anywhere and has to be dynamically positioned right after accountingDate everytime
     * condition accountingDatesTimeStamp on accountingDate and pass to a rule which applies this to the
     * Params example using above as an example
     *
     * @param sourceColumnConfigs columnConfigsGenerated. usually conditioning should only happen once base columns are generated
     * @param conditionedOnColumn accountingDate column config from above example
     * @param columnOnWhichToApplyCondition accountingDatesTimeStamp column config from above example
     * @return modifies passed list and returns List<ColumnConfig> which has accountingDatesTimeStamp column attached on the
     * right of accountingDate column if accountingDate column exists
     *
     */
    List<ColumnConfig> applyRule(List<ColumnConfig> sourceColumnConfigs,
                             ColumnConfig conditionedOnColumn, ColumnConfig columnOnWhichToApplyCondition );



}
