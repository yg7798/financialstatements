package com.tekion.accounting.fs.service.helper.excelGeneration.apiGateway;


public interface LambdaWarmUpService {

    long warmUpExcelGenerationLambda(Integer numOfInstances);
}
