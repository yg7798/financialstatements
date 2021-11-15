package com.tekion.accounting.fs.service.helper.excelGeneration.columnConfigs.context;

import com.tekion.accounting.fs.service.helper.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.service.helper.excelGeneration.columnConfigs.enums.PreferenceColumnMappingType;
import com.tekion.accounting.fs.service.helper.excelGeneration.enums.ExcelReportSheet;
import com.tekion.accounting.fs.service.helper.excelGeneration.enums.ExcelReportType;
import com.tekion.core.excelGeneration.models.enums.GeneratorVersion;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColumnConfigComputeContext {

    private String reportType;
    private ExcelReportType excelReportType;


    private ExcelReportSheet sheet;
    private GeneratorVersion generatorVersion;

    private boolean leftAlignGroupByColumns = true;

    private boolean isSummaryView;
    private boolean isDetailView;
    private boolean isPlainView;

    private boolean generatePreferenceBasedDynamicColumn;


    private Class<? extends AccAbstractColumnConfig> groupByColumnConfigs;
    private Class<? extends AccAbstractColumnConfig> baseColumnConfigs;


    private AccAbstractColumnConfig groupByEnumVal;


    private PreferenceColumnMappingType preferenceColumnMappingType = PreferenceColumnMappingType.ONE_TO_ONE;
    private String overriddenPreferenceAssetType ;
    private boolean overridePreferenceAssetType=false ;
    private List<ColumnConfig> listOfBaseColumnConfigsToBeUsed;

}
