package com.tekion.accounting.fs.service.oemPayload;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.integration.ProcessFinancialStatement;
import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.integration.Detail;
import com.tekion.accounting.fs.integration.FinancialStatement;
import com.tekion.accounting.fs.integration.Header;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HondaBrandFinancialStatementServiceImpl extends AbstractFinancialStatementService {

    public HondaBrandFinancialStatementServiceImpl(DealerConfig dc, IntegrationClient ic, FsXMLServiceImpl fs) {
        super(dc, ic, fs);
    }

    @Override
    public String generateXML(FinancialStatementRequestDto requestDto) {
        throw new TBaseRuntimeException(AccountingError.notSupported);
    }

    /**
     * Generates STAR format file
     * */
    @Override
    public void downloadFile(FinancialStatementRequestDto requestDto, HttpServletResponse response) {
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());
        ProcessFinancialStatement pfs = getFinancialStatementResponse(requestDto);

        Map<String, FinancialStatement> fsMap = pfs.getDataArea().getFinancialStatements().stream()
            .collect(Collectors.toMap(x-> x.getHeader().getAccountingTerm(), x -> x));
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=AMDSHOFS.007");
        try{
            writeHeader(fsMap.get("YTD").getHeader(), response.getWriter(), requestDto.getTillEpoch(), fsEntry.getOemId());
            writeAccountDetails(fsMap.get("YTD"), response.getWriter());
            writeExtras(response.getWriter());
        } catch (IOException e){
            log.error("Error while downloading Financial Statement", e);
            throw new TBaseRuntimeException(AccountingError.ioError);
        }
    }

    /**
     * FS.DETAIL:M2220,0.00
     * */
    private void writeAccountDetails(FinancialStatement fs, PrintWriter writer){
        for(Detail detail: fs.getDetails()){
            writer.println("FS.DETAIL:"+detail.getAccountId()+","+detail.getAccountValue());
        }
    }

    /**
     * FS.IDENT:AD,"ELITE",2.4,20200609,181600,251003,,,HO,,
     * FS.HEADER:202006,YTD,852
     * */

    private void writeHeader(Header header, PrintWriter writer, long tillEpoch, String oemId){

        Date date = new Date(tillEpoch);
        DateFormat format = new SimpleDateFormat("yyyyMMdd HHmmss");
        format.setTimeZone(TimeZone.getTimeZone(dealerConfig.getDealerTimeZoneName()));
        String[] format1 = format.format(date).split(" ");

        String s = String.format("FS.IDENT:AD,\"ELITE\",2.4,%s,%s,%s,,,HO,,",format1[0], format1[1], header.getDealerCode());
        writer.println(s);
        int recordsCount = Integer.parseInt(header.getCount());

        if(OEM.Honda.name().equals(oemId)){
            recordsCount += 2;
        }

        writer.println(String.format("FS.HEADER:%s,%s,%s", header.getAccountingDate().replace("-", ""), header.getAccountingTerm(), recordsCount));
    }

    /**
     * FS.DETAIL:chkBSD,N
     * FS.DETAIL:chkNOD,N
     * */
    private void writeExtras(PrintWriter writer){
        AccountingInfo info = infoService.find(UserContextProvider.getCurrentDealerId());
        String bsd = String.format("FS.DETAIL:chkBSD,%s", Objects.isNull(info) || !info.isBsdPresent() ? "N": "Y");
        String nod = String.format("FS.DETAIL:chkNOD,%s", Objects.isNull(info) || !info.isNodPresent() ? "N": "Y");
        writer.println(bsd);
        writer.println(nod);
    }
}
