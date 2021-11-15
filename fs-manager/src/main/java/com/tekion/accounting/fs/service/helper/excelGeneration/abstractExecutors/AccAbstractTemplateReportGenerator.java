package com.tekion.accounting.fs.service.helper.excelGeneration.abstractExecutors;

import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.service.helper.excelGeneration.apiGateway.IApiGatewayReportConfig;
import com.tekion.accounting.fs.service.helper.excelGeneration.dto.AccExcelRequestDto;
import com.tekion.accounting.fs.service.helper.excelGeneration.helper.ExcelReportGeneratorHelper;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.core.excelGeneration.helpers.models.template.AbstractExcelTemplateReportGenerator;
import com.tekion.core.excelGeneration.models.model.CallbackAcknowledge;
import com.tekion.core.excelGeneration.models.model.ErrorCallBackResponse;
import com.tekion.core.excelGeneration.models.model.SuccessCallbackResponse;
import com.tekion.core.excelGeneration.models.model.template.ExcelTemplateRequestDto;
import com.tekion.notificationsv2.client.NotificationsV2Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AccAbstractTemplateReportGenerator extends AbstractExcelTemplateReportGenerator implements IApiGatewayReportConfig {

    @Autowired
    private DealerConfig dealerConfig;

    @Autowired
    protected NotificationsV2Client notificationClient;

    @Autowired
    private ExcelReportGeneratorHelper helper;

    protected abstract ExcelTemplateRequestDto createExcelTemplateDto(AccExcelRequestDto payload, String reportType);

    @Override
    protected String getServiceBaseUrlPrefix() {
        return "/accounting/u";
    }

    @Override
    protected String getServiceName() {
        return TConstants.SERVICE_NAME_ACCOUNTING;
    }

    @Override
    protected String getDealerTimeZoneName() {
        return dealerConfig.getDealerTimeZoneName();
    }

    @Override
    public ExcelTemplateRequestDto createExcelTemplateRequestDto(Object payload, String reportType) {
         return doCreateExcelTemplateDto(JsonUtil.fromJson(JsonUtil.toJson(payload), AccExcelRequestDto.class).orElse(null),reportType);
    }

    private ExcelTemplateRequestDto doCreateExcelTemplateDto(AccExcelRequestDto requestDto, String reportType) {
        ExcelTemplateRequestDto templateRequestDto = createExcelTemplateDto(requestDto, reportType);
        return templateRequestDto;
    }

    @Override
    public CallbackAcknowledge doOnErrorCallback(ErrorCallBackResponse request, String reportType) {
        return helper.doOnErrorCallback(request,reportType);
    }

    @Override
    public CallbackAcknowledge doOnSuccessCallback(SuccessCallbackResponse request, String reportType) {
        return helper.doOnSuccessCallback(request,reportType);
    }
}
