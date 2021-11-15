package com.tekion.accounting.fs.common.excelGeneration.helper;

import com.tekion.accounting.fs.common.excelGeneration.columnConfigs.context.DynamicallyUpdateColumnConfigContext;
import com.tekion.accounting.fs.common.excelGeneration.dto.EsReportRequestDto;
import com.tekion.accounting.fs.common.excelGeneration.enums.ExcelReportType;
import com.tekion.core.excelGeneration.models.CallbackResponse;
import com.tekion.core.excelGeneration.models.model.*;

import java.util.List;

public interface ExcelReportGeneratorHelper {
    public CallbackAcknowledge doOnErrorCallback(ErrorCallBackResponse request, String reportType);
    public CallbackAcknowledge doOnSuccessCallback(SuccessCallbackResponse request, String reportType);
//    void sendNotification(ExcelReportType reportType, ErrorCallBackResponse errorCallBackResponse);
//    void sendNotification(ExcelReportType reportType, SuccessCallbackResponse successCallbackResponse);
    String determineCorrespondingSortKeyStringToSend(ExcelReportType excelReportType, String fieldForWhichToFindMapping);
    List<Sort> createSortToBeDoneOnLambda(String reportType, EsReportRequestDto requestDto, boolean doHandleForAmountSort);

    void doDynamicallyUpdateColumnFormatting(List<ColumnConfig> columnConfigList, DynamicallyUpdateColumnConfigContext dynamicallyUpdateColumnConfigContext);

    void doDynamicallyUpdateLocalizedFieldName(List<ColumnConfig> columnConfigList);

    DynamicallyUpdateColumnConfigContext doGenerateContextForDynamicUpdate(String reportType);

    ColumnConfig getCopyOfColumnConfig(ColumnConfig columnConfig);
    //void populateUserLookupAssetMapInContextV2(ExcelReportContextV2 context, Set<String> userIdList, ResolveAssetUtils resolveAssetUtils, LookupAsset lookUpAsset);

    //Map<String, String> createUserIdVsUserNameMap(Set<String> userIdList);

    Object getSyncResponseForPdfPreview(CallbackResponse response, String reportType, Object originalPayload);
}
