package com.tekion.accounting.fs.service.common.excelGeneration.apiGateway;

// so every report can specify their own expected syncResponseMaxTime
public interface IApiGatewayReportConfig {

    default int expectedResponseTimeInMillis(Object payload, String reportType){
        return 5_000;
    }
}
