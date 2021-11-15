package com.tekion.accounting.fs.common.excelGeneration.apiGateway;


public interface LambdaWarmUpService {

    long warmUpExcelGenerationLambda(Integer numOfInstances);
}
