package com.tekion.accounting.fs.service.common.excelGeneration.abstractExecutors;

import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.core.minimisedResource.MinimizedResourceMetaData;
import com.tekion.accounting.fs.service.common.excelGeneration.apiGateway.IApiGatewayReportConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.ExcelColumnConfigGeneratorService;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.context.ColumnConfigComputeContext;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.AccExcelRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportType;
import com.tekion.accounting.fs.service.common.excelGeneration.helper.ExcelReportGeneratorHelperImpl;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.core.excelGeneration.helpers.models.AbstractExcelReportGenerator;
import com.tekion.core.excelGeneration.models.CallbackResponse;
import com.tekion.core.excelGeneration.models.enums.GeneratorVersion;
import com.tekion.core.excelGeneration.models.model.*;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.notificationsv2.client.NotificationsV2Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public abstract class AccAbstractExcelReportGenerator extends AbstractExcelReportGenerator implements IApiGatewayReportConfig {

    @Autowired
    private ExcelColumnConfigGeneratorService excelColumnConfigGeneratorService;

    private final int MAX_ALLOWED_BATCH_SIZE = 5000;
    private final int EXEMPTION_MAX_UPPER_LIMIT_BATCH_SIZE = 9999;

    @Autowired
    // composition over inheritance
    private ExcelReportGeneratorHelperImpl helper;
    @Autowired
    protected PreferenceClient preferenceClient;

    @Autowired
    protected NotificationsV2Client notificationClient;

    protected abstract ExcelGenerationRequestDto createExcelGenerationDto(AccExcelRequestDto payload, String reportType);

    //overrideThisToEnableDynamicColumnGeneration
    protected boolean enablePreferenceBasedDynamicColumns(String reportType) {
        return false;
    }

    //override this for alignment of groupByColumns
    protected boolean leftAlignGroupByColumns(String reportType) {
        return true;
    }

    @Autowired
    private DealerConfig dealerConfig;
    //toEnforceCertainServiceWideCommonParams

    private ExcelGenerationRequestDto doCreateExcelGenerationDto(AccExcelRequestDto payload, String reportType) {
        ExcelGenerationRequestDto excelGenerationDto = createExcelGenerationDto(payload,reportType);
        int batchSize = excelGenerationDto.getBatchSize();

        if(exemptionForUpperBatchSizeLimit()){
            if(batchSize > EXEMPTION_MAX_UPPER_LIMIT_BATCH_SIZE){
                excelGenerationDto.setBatchSize(EXEMPTION_MAX_UPPER_LIMIT_BATCH_SIZE);

            }
        }
        else if(batchSize > MAX_ALLOWED_BATCH_SIZE) {
            excelGenerationDto.setBatchSize(MAX_ALLOWED_BATCH_SIZE);
        }

        if(enablePreferenceBasedDynamicColumns(reportType) && TCollectionUtils.isEmpty(excelGenerationDto.getColumnConfigList())){
            //probably the only cause
            throw new TBaseRuntimeException(FSError.unableToDetermineColumnForReportGeneration);
        }
        if(TCollectionUtils.isEmpty(excelGenerationDto.getColumnConfigList())){
            throw new TBaseRuntimeException();
        }
        return excelGenerationDto;
    }

    protected void doDynamicallyUpdateFormatsOfColumnConfigs(ExcelGenerationRequestDto requestDto, String reportType) {
        helper.doDynamicallyUpdateColumnFormatting(requestDto.getColumnConfigList(), helper.doGenerateContextForDynamicUpdate(reportType));
    }

    protected Map<String, AccAbstractColumnConfig> getSortKeyToEnumMapForReportType(String reportType){
        ExcelReportType excelReportType = ExcelReportType.valueOf(reportType);
        if(Objects.nonNull(excelReportType.getBaseColumnConfigs())){
            return AccAbstractColumnConfig.getSortKeyToEnumMap(excelReportType.getBaseColumnConfigs().getName());
        }
        if(Objects.nonNull(excelReportType.getGroupByColumnConfigs())){
            return AccAbstractColumnConfig.getSortKeyToEnumMap(excelReportType.getGroupByColumnConfigs().getName());
        }
        return null;
    }


    @Override
    public List<ColumnConfig> getColumnConfigForReport(String reportType) {
        return excelColumnConfigGeneratorService.columnConfigGenerator(getContextForColumnConfig(reportType));
    }

    protected boolean exemptionForUpperBatchSizeLimit(){
        return false;
    }

    @Override
    public ExcelGenerationRequestDto createExcelGenerationDto(Object payload, String reportType) {
        return doCreateExcelGenerationDto(JsonUtil.fromJson(JsonUtil.toJson(payload), AccExcelRequestDto.class).orElse(null),reportType);
    }

    @Override
    public Object doGetResponseInSyncAndReturn(CallbackResponse response, String reportType, Object originalPayload){
        return helper.getSyncResponseForPdfPreview(response,reportType,originalPayload);
    }

    @Override
    public int getDefaultBatchSize() {
        return TConstants.DEFAULT_BATCH_SIZE_FOR_EXCEL;
    }


    @Override
    protected String getServiceBaseUrlPrefix() {
        return "/financial-statements/u";
    }

    @Override
    protected String getServiceName() {
        return TConstants.SERVICE_NAME_FINANCIAL_STATEMENTS;
    }


    @Override
    protected String getDealerTimeZoneName() {
        return dealerConfig.getDealerTimeZoneName();
    }

    @Override
    public CallbackAcknowledge doOnErrorCallback(ErrorCallBackResponse request, String reportType) {
        return helper.doOnErrorCallback(request, reportType);
    }

    @Override
    public CallbackAcknowledge doOnSuccessCallback(SuccessCallbackResponse request, String reportType) {
        return helper.doOnSuccessCallback(request, reportType);
    }

    protected MinimizedResourceMetaData getMinimizeMetadata(ExcelGenerationRequestDto excelGenerationRequestDto) {
        return MinimizedResourceMetaData.builder().fields(excelGenerationRequestDto.getColumnConfigList().stream().map(ColumnConfig::getKey).collect(Collectors.toSet()))
                .includeType(MinimizedResourceMetaData.IncludeType.INCLUSION)
                .addMinimizedFlag(false)
                .build();
    }

    protected void fixOrderOfColumnsInGeneratedList(List<ColumnConfig> columnConfigList) {
        excelColumnConfigGeneratorService.fixOrderOfColumnsInGeneratedList(columnConfigList);
    }

    private ColumnConfigComputeContext getContextForColumnConfig(String reportType) {
        ColumnConfigComputeContext columnConfigComputeContext = new ColumnConfigComputeContext();
        columnConfigComputeContext.setReportType(reportType);
        columnConfigComputeContext.setGeneratePreferenceBasedDynamicColumn(enablePreferenceBasedDynamicColumns(reportType));
        columnConfigComputeContext.setLeftAlignGroupByColumns(leftAlignGroupByColumns(reportType));
        columnConfigComputeContext.setGeneratorVersion(GeneratorVersion.SINGLE_SHEET);
        return columnConfigComputeContext;
    }
}
