package com.tekion.accounting.fs.excelGeneration.apiGateway;


public interface LambdaWarmUpService {

    long warmUpExcelGenerationLambda(Integer numOfInstances);
}
