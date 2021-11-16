package com.tekion.accounting.fs.service.common.excelGeneration.apiGateway;


public interface LambdaWarmUpService {

    long warmUpExcelGenerationLambda(Integer numOfInstances);
}
