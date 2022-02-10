package com.tekion.accounting.fs.service.oems;


import com.tekion.accounting.fs.integration.ProcessFinancialStatement;
import com.tekion.accounting.fs.integration.Detail;
import com.tekion.accounting.fs.integration.FinancialStatement;
import com.tekion.accounting.fs.integration.Header;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KiaFSServiceImpl extends AbstractFinancialStatementService {

    public KiaFSServiceImpl(DealerConfig dc, IntegrationClient ic, FsXMLServiceImpl fs) {
        super(dc, ic, fs);
    }

    @Override
    public String generateXML(FinancialStatementRequestDto requestDto) {
        throw new TBaseRuntimeException(AccountingError.notSupported);
    }

    /**
     * Generates STAR format file
     *
     * @param requestDto
     * @param response */
    @Override
    public void downloadFile(FinancialStatementRequestDto requestDto, HttpServletResponse response) {

        ProcessFinancialStatement pfs = getFinancialStatementResponse(requestDto);
        Map<String, FinancialStatement> fsMap = pfs.getDataArea().getFinancialStatements().stream()
            .collect(Collectors.toMap(x-> x.getHeader().getAccountingTerm(), x -> x));

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=Kia.txt");
        try{
            writeHeader(fsMap.get("YTD").getHeader(), fsMap.get("MTD").getHeader(), response.getWriter(), requestDto.getTillEpoch());
            writeAccountDetails(fsMap.get("YTD"), response.getWriter());
            writeAccountDetails(fsMap.get("MTD"), response.getWriter());
            writeExtras(response.getWriter(), fsMap.get("YTD").getHeader(), fsMap.get("MTD").getHeader());
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
     * FS.IDENT:XX,XXX,XXX.X,20191119,075550,12345,11,01,KI,DCS,N,USA,,RR12345,00000000110000000001,Accounting,,,,
     * FS.HEADER:202009,YTD,1775
     * */

    private void writeHeader(Header ytdHeader, Header mtdHeader, PrintWriter writer, long tillEpoch){

        Date date = new Date(tillEpoch);
        DateFormat format = new SimpleDateFormat("yyyyMMdd HHmmss");
        format.setTimeZone(TimeZone.getTimeZone(dealerConfig.getDealerTimeZoneName()));
        String[] dateTime = format.format(date).split(" ");

        String firstLine = String.format("FS.IDENT:XX,XXX,XXX.XX,%s,%s,%s,11,01,KI,DCS,N,USA,,RR12345,00000000110000000001,Accounting,,,,"
            ,dateTime[0], dateTime[1], ytdHeader.getDealerCode());

        writer.println(firstLine);
        writer.println(String.format("FS.HEADER:%s,%s,%s", ytdHeader.getAccountingDate().replace("-", ""),
                ytdHeader.getAccountingTerm(), ytdHeader.getCount()+mtdHeader.getCount()));
    }

    /**
     * FS.TRAILER:1776
     * */
    private void writeExtras(PrintWriter writer, Header ytdHeader, Header mtdHeader){
        String lastLine = String.format("FS.TRAILER: %d", Integer.parseInt(ytdHeader.getCount()+mtdHeader)+1);
        writer.println(lastLine);
    }
}
