package com.tekion.accounting.fs.service.oemPayload;

import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.accounting.fs.beans.ProcessFinancialStatement;
import com.tekion.accounting.fs.dto.FSTemplateRequestValidationGroup;
import com.tekion.accounting.fs.dto.integration.FSSubmitResponse;
import com.tekion.accounting.fs.dto.oemPayload.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.core.excelGeneration.models.model.template.SingleCellData;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.core.validation.TValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Slf4j
@Component
@AllArgsConstructor
@Primary
public class FSReportServiceImpl implements FinancialStatementService{
    private GMFinancialStatementServiceImpl gmFinancialStatementService;
    private ToyotaFinancialStatementServiceImpl toyotaFinancialStatementService;
    private HondaBrandFinancialStatementServiceImpl hondaBrandFSService;
    private DefaultFSServiceImpl defaultFSService;
    private KiaFSServiceImpl KiaFSService;
    private FordServiceImpl fordService;
    private MazdaFSService mazdaService;
    private FSEntryRepo fsEntryRepo;
    private TValidator validator;


    @Override
    public String generateXML(FinancialStatementRequestDto requestDto) {
        validator.validate(requestDto, FSTemplateRequestValidationGroup.class);
        return getForOem(requestDto.getFsId()).generateXML(requestDto);
    }

    @Override
    public FSSubmitResponse submit(FinancialStatementRequestDto requestDto) {
        validator.validate(requestDto, FSTemplateRequestValidationGroup.class);
        return getForOem(requestDto.getFsId()).submit(requestDto);
    }

    @Override
    public ProcessFinancialStatement getStatement(FinancialStatementRequestDto requestDto) {
        validator.validate(requestDto, FSTemplateRequestValidationGroup.class);
        return getForOem(requestDto.getFsId()).getStatement(requestDto);
    }

    @Override
    public void downloadFile(FinancialStatementRequestDto requestDto, HttpServletResponse response) {
        validator.validate(requestDto, FSTemplateRequestValidationGroup.class);
        getForOem(requestDto.getFsId()).downloadFile(requestDto, response);
    }

    @Override
    public List<SingleCellData> getCellLevelFSReportData(FinancialStatementRequestDto requestDto) {
        return getForOem(requestDto.getFsId()).getCellLevelFSReportData(requestDto);
    }

    FinancialStatementService getForOem(String fsId){
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
        OEM oem = OEM.valueOf(fsEntry.getOemId());
        switch (oem){
            case GM:
                return gmFinancialStatementService;
            case Toyota:
                return toyotaFinancialStatementService;
            case Acura:
            case Honda:
                return hondaBrandFSService;
            case Kia:
                return KiaFSService;
            case Ford:
                return fordService;
            case Mazda:
                return mazdaService;
            default:
                log.info("{} using defaultFSService", oem.name());
                return defaultFSService;
        }
    }
}
