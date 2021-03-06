package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DefaultFSServiceImpl extends AbstractFinancialStatementService{

    public DefaultFSServiceImpl(DealerConfig dealerConfig, IntegrationClient integrationClient, FsXMLServiceImpl fsService) {
        super(dealerConfig, integrationClient, fsService);
    }

    @Override
    public String generateXML(FinancialStatementRequestDto requestDto) {
        throw new TBaseRuntimeException(FSError.notSupported);
    }
}
