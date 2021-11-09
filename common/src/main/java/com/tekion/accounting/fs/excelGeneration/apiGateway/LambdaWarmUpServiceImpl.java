package com.tekion.accounting.fs.excelGeneration.apiGateway;

import com.tekion.accounting.fs.dpProvider.DpUtils;
import com.tekion.clients.excelGeneration.client.ExcelGenerationClient;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@RequiredArgsConstructor
public class LambdaWarmUpServiceImpl  implements LambdaWarmUpService{

    private final ExcelGenerationClient client;

    @Override
    public long warmUpExcelGenerationLambda(Integer numOfInstances) {
        int numOfInstancesToUse = numOfInstances ==null? DpUtils.getExcelGenerationLAPiGatewayLambdaWarmInstanceCount() : numOfInstances;
        if(numOfInstancesToUse > 30){
            log.error("CRITICAL ERROR  : num of instances to warm up requested > 30 : resetting to 30 {}", numOfInstancesToUse);
            numOfInstancesToUse = 30;
        }
        try {
            return client.warmUpLambda(numOfInstancesToUse);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while warming up excel generationLambdaApiGateway : check on priority {} ",e);
            throw new TBaseRuntimeException();
        }

    }
}
