package com.tekion.accounting.fs.service.common.excelGeneration.abstractExecutors;


import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.apiGateway.IApiGatewayReportConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.context.ColumnConfigComputeContext;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.ExcelColumnConfigGeneratorService;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.AccExcelRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.SheetInfoDto;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportSheet;
import com.tekion.accounting.fs.service.common.excelGeneration.helper.ExcelReportGeneratorHelper;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.core.excelGeneration.helpers.models.v2.AbstractExcelReportGeneratorV2;
import com.tekion.core.excelGeneration.models.CallbackResponse;
import com.tekion.core.excelGeneration.models.enums.GeneratorVersion;
import com.tekion.core.excelGeneration.models.model.*;
import com.tekion.core.excelGeneration.models.model.v2.ExcelGenerationRequestDtoV2;
import com.tekion.core.excelGeneration.models.model.v2.SheetDetails;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public abstract class AccAbstractExcelReportGeneratorV2 extends AbstractExcelReportGeneratorV2 implements IApiGatewayReportConfig {

    @Autowired
    private ExcelReportGeneratorHelper helper;

    @Autowired
    private ExcelColumnConfigGeneratorService excelColumnConfigGeneratorService;


    @Autowired
    private DealerConfig dealerConfig;

    abstract public List<String> supportedReportNames() ;

    public abstract ExcelGenerationRequestDtoV2 createExcelGenerationDto(AccExcelRequestDto requestDto, String reportType);

    //overrideThisToEnableDynamicColumnGeneration
    protected boolean enablePreferenceBasedDynamicColumns(String reportType) {
        return false;
    }

    private ExcelGenerationRequestDtoV2 doCreateExcelGenerationDto(AccExcelRequestDto payload, String reportType) {
        List<ExcelReportSheet> excelReportSheets = ExcelReportSheet.getReportTypeToGroupInfoHolder().get(reportType);
        if(com.tekion.core.excelGeneration.models.utils.TCollectionUtils.isEmpty(excelReportSheets)){
            log.error("sheets not registered for the report type : {}", reportType);
            throw new TBaseRuntimeException("sheets not registered for the report type : {}", reportType);
        }
        ExcelGenerationRequestDtoV2 excelGenerationDto = createExcelGenerationDto(payload,reportType);
        return excelGenerationDto;
    }
    @Override
    public ExcelGenerationRequestDtoV2 createExcelGenerationDto(Object payload, String reportType) {
        return doCreateExcelGenerationDto(JsonUtil.fromJson(JsonUtil.toJson(payload), AccExcelRequestDto.class).orElse(null),reportType);
    }

    // for legacy support
    protected List<SheetDetails> doGenerateSheetDetailsFromV1Dto(ExcelGenerationRequestDto excelGenerationRequestDto){
        return ExcelGenerationRequestDto.toV2RequestDto(excelGenerationRequestDto).getSheetDetails();
    }

    @Override
    public Object doGetResponseInSyncAndReturn(CallbackResponse response, String reportType, Object originalPayload){
        return helper.getSyncResponseForPdfPreview(response,reportType,originalPayload);
    }


    protected void doDynamicallyUpdateFormatsOfColumnConfigs(ExcelGenerationRequestDtoV2 requestDtoV2, String reportType) {
        for (SheetDetails sheetDetail : TCollectionUtils.nullSafeList(requestDtoV2.getSheetDetails())) {
            List<ColumnConfig> columnConfigList = sheetDetail.getColumnConfigList();
            helper.doDynamicallyUpdateLocalizedFieldName(columnConfigList);
            helper.doDynamicallyUpdateColumnFormatting(columnConfigList, helper.doGenerateContextForDynamicUpdate(reportType));
        }
    }

    protected void doDynamicallyUpdateLocalizedColumnConfig(List<ColumnConfig> columnConfigList) {
        helper.doDynamicallyUpdateLocalizedFieldName(columnConfigList);
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
        return helper.doOnErrorCallback(request,reportType);
    }

    public List<ColumnConfig> getDefaultColumnConfigList(String reportType) {
        List<ExcelReportSheet> excelReportSheets = ExcelReportSheet.getReportTypeToGroupInfoHolder().get(reportType);
        ExcelReportSheet excelReportSheet = excelReportSheets.get(0);
        List<ColumnConfig> columnConfigList = AccAbstractColumnConfig.getEnumList(excelReportSheet.getBaseColumnConfigs().getName())
                .stream().map(AccAbstractColumnConfig::toColumnConfig).collect(Collectors.toList());
        fixOrderOfColumnsInGeneratedList(columnConfigList);
        return columnConfigList;
    }

    public SheetDetails getSheetDetails(String reportType, ExcelReportSheet excelReportSheet, boolean isPaginated,int batchSize){
        List<ColumnConfig> columnConfigList = getDefaultColumnConfigList(reportType);
        final SheetDetails sheetDetail;
        if(isPaginated)
           sheetDetail = ExcelReportSheet.toPaginatedSheetDetails(excelReportSheet);
        else
           sheetDetail = ExcelReportSheet.toUnPaginatedSheetDetails(excelReportSheet);
        sheetDetail.setBatchSize(batchSize);
        sheetDetail.setColumnConfigList(columnConfigList);
        return sheetDetail;
    }

    public Set<SheetInfoDto> getSheetInfoSet(SheetDetails sheetDetail)
    {
        Set<SheetInfoDto> sheetInfoSet = new HashSet<>();
        SheetInfoDto correspondentSheetInfoDto = getSheetInfoDtoForSheetDetails(sheetDetail);
        correspondentSheetInfoDto.setComputedColumnConfigList(sheetDetail.getColumnConfigList());
        sheetInfoSet.add(correspondentSheetInfoDto);

        return sheetInfoSet;
    }

    private SheetInfoDto getSheetInfoDtoForSheetDetails(SheetDetails sheetDetail) {
        SheetInfoDto sheetInfoDto  = new SheetInfoDto();
        sheetInfoDto.setSheetIdentifier(sheetDetail.getSheetIdentifier());
        return sheetInfoDto;
    }
    protected void fixOrderOfColumnsInGeneratedList(List<ColumnConfig> columnConfigList) {
        excelColumnConfigGeneratorService.fixOrderOfColumnsInGeneratedList(columnConfigList);
    }
    @Override
    public CallbackAcknowledge doOnSuccessCallback(SuccessCallbackResponse request, String reportType) {
        return helper.doOnSuccessCallback(request,reportType);
    }

    public List<ColumnConfig> getColumnConfigForReport(String reportType, ExcelReportSheet sheet) {
        return excelColumnConfigGeneratorService.columnConfigGenerator(getContextForColumnConfig(reportType, sheet));
    }

    protected List<ColumnConfig> getColumnConfigForReport( ColumnConfigComputeContext columnConfigComputeContext) {
        return excelColumnConfigGeneratorService.columnConfigGenerator(columnConfigComputeContext);
    }

    protected ColumnConfigComputeContext getContextForColumnConfig(String reportType, ExcelReportSheet sheet) {
        ColumnConfigComputeContext columnConfigComputeContext = new ColumnConfigComputeContext();
        columnConfigComputeContext.setReportType(reportType);
        columnConfigComputeContext.setGeneratePreferenceBasedDynamicColumn(enablePreferenceBasedDynamicColumns(reportType));
        columnConfigComputeContext.setGeneratorVersion(GeneratorVersion.MULTI_SHEET);
        columnConfigComputeContext.setSheet(sheet);
        return columnConfigComputeContext;
    }

}
