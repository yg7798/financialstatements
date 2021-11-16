package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.integration.ProcessFinancialStatement;
import com.tekion.accounting.fs.dto.integration.FSSubmitResponse;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.core.excelGeneration.models.model.template.SingleCellData;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface FinancialStatementService {
    String generateXML(FinancialStatementRequestDto requestDto);

    FSSubmitResponse submit(FinancialStatementRequestDto requestDto);

    ProcessFinancialStatement getStatement(FinancialStatementRequestDto requestDto);

    void downloadFile(FinancialStatementRequestDto requestDto, HttpServletResponse response);

    List<SingleCellData> getCellLevelFSReportData(FinancialStatementRequestDto financialStatementRequestDto);
}
