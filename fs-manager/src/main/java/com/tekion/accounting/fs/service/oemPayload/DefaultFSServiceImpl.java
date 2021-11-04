package com.tekion.accounting.fs.service.oemPayload;

import com.tekion.accounting.fs.dto.oemPayload.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.accounting.fs.utils.DealerConfig;
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
        throw new TBaseRuntimeException(AccountingError.notSupported);
    }
}
