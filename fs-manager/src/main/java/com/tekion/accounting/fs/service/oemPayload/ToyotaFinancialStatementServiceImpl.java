package com.tekion.accounting.fs.service.oemPayload;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.integration.ProcessFinancialStatement;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

@Component
@Slf4j
public class ToyotaFinancialStatementServiceImpl extends AbstractFinancialStatementService{

    public ToyotaFinancialStatementServiceImpl(DealerConfig dealerConfig,
                                               IntegrationClient integrationClient, FsXMLServiceImpl fsService) {
        super(dealerConfig, integrationClient, fsService);
    }

    @Override
    public String generateXML(FinancialStatementRequestDto requestDto) {
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());
        log.info("FS: Generating XML {} {} {} {} ", requestDto.getFsId(), fsEntry.getOemId(), fsEntry.getYear(), requestDto.getTillEpoch());
        ProcessFinancialStatement processFinancialStatement = super.getFinancialStatementResponse(requestDto);
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(ProcessFinancialStatement.class);
            Marshaller marshaller = jaxbContext.createMarshaller();

            StringWriter writer = new StringWriter();
            marshaller.marshal(processFinancialStatement, writer);
            return writer.toString();
        } catch (JAXBException e) {
            log.error("Exception occurred  ",e);
        }

        return "Invalid response";
    }

}
