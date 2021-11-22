package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.integration.Detail;
import com.tekion.accounting.fs.integration.FinancialStatement;
import com.tekion.accounting.fs.integration.ProcessFinancialStatement;
import com.tekion.accounting.fs.service.external.nct.NCTRow;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class GMFinancialStatementServiceImpl extends AbstractFinancialStatementService{

    public static final int NCB_STATEMENT_YEAR = 0;
    public static final List<String> zeroValues = Arrays.asList("0", "0.0", "0.00");

    public GMFinancialStatementServiceImpl(DealerConfig dc, IntegrationClient ic, FsXMLServiceImpl fs) {
        super(dc, ic, fs);
    }

    @Override
    public String generateXML(FinancialStatementRequestDto requestDto) {
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());
        log.info("FS: Generating XML {} {} {} {} ", fsEntry.getId(), fsEntry.getOemId(), fsEntry.getYear(), requestDto.getTillEpoch());
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

    @Override
    public void downloadFile(FinancialStatementRequestDto requestDto, HttpServletResponse response) {
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());

        if(Objects.nonNull(fsEntry) && NCB_STATEMENT_YEAR == fsEntry.getYear()){
            generateNCTFile(requestDto, response);
            return;
        }

        String report = generateXML(requestDto);;
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment;filename=thisIsTheFileName.xml");
        try{
            response.getWriter().write(report);
        }catch (IOException ex) {
            log.error("IoException while generating XML file", ex);
            throw new TBaseRuntimeException(AccountingError.ioError);
        }
    }

    private void generateNCTFile(FinancialStatementRequestDto requestDto, HttpServletResponse response) {
        ProcessFinancialStatement processFinancialStatement = super.getFinancialStatementResponse(requestDto);
        List<NCTRow> nctRows = createNCTRows(processFinancialStatement);
        WriteValuesToWorkbook(nctRows, response);
    }

    private void WriteValuesToWorkbook(List<NCTRow> nctRows, HttpServletResponse response) {
        String fileName = "nctFile.xlsx";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("NCT");

            // Create a header row describing what the columns mean
            CellStyle boldStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            boldStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            addCells(NCTRow.getHeaderRow(), headerRow, boldStyle);
            log.info("nctRows size"+nctRows.size());

            for (int i = 0; i < nctRows.size(); i++) {
                // Add one due to the header row
                Row row = sheet.createRow(i + 1);
                NCTRow nctRow = nctRows.get(i);
                addCells(nctRow, row, null);
            }
            workbook.write(response.getOutputStream());
            workbook.dispose();
            response.getOutputStream().close();
            response.setHeader("Content-disposition","attachment; filename=" + fileName);

        } catch (IOException e) {
            System.err.println("Could not create XLSX file at " + fileName);
            e.printStackTrace();
        }
    }

    private static void addCells(NCTRow nctRow, Row row, CellStyle style) {

        Cell locCell = row.createCell(0, CellType.STRING);
        locCell.setCellValue(nctRow.getLocation());
        if(Objects.nonNull(style)) locCell.setCellStyle(style);

        Cell amtCell = row.createCell(1, CellType.STRING);
        if(isZeroValue(nctRow.getAmount())){
            amtCell.setCellValue(TConstants.BLANK_STRING);
        }else{
            amtCell.setCellValue(nctRow.getAmount());
        }

        if(Objects.nonNull(style)) amtCell.setCellStyle(style);

        Cell dateCell = row.createCell(2, CellType.NUMERIC);
        dateCell.setCellValue(nctRow.getMmyy());
        if(Objects.nonNull(style)) dateCell.setCellStyle(style);
    }

    static boolean isZeroValue(String s){
        return zeroValues.contains(s);
    }

    private List<NCTRow> createNCTRows(ProcessFinancialStatement processFinancialStatement) {
        List<NCTRow> nctRows = new ArrayList<>();
        String accountingDate = processFinancialStatement.getDataArea().getFinancialStatements().get(0).getHeader().getAccountingDate();
        String nctDate = getDateForNctFormat(accountingDate);
        List<FinancialStatement> financialStatements = processFinancialStatement.getDataArea().getFinancialStatements();
        for(FinancialStatement fs: TCollectionUtils.nullSafeList(financialStatements)){
            List<Detail> details = TCollectionUtils.nullSafeList(fs.getDetails());
            for(Detail d: details){
                nctRows.add(NCTRow.getNctRow(d, nctDate));
            }
        }
        return nctRows;
    }

    /**
     * DATE format for NCT is MMyy i.e, for May 2021 its 0521
     * */
    String getDateForNctFormat(String yyyyxMM){
        return yyyyxMM.substring(5) + yyyyxMM.substring(2,4);
    }

    protected String formatTBin(AccountingOemFsCellCode cellCode) {
        String tbinStr = cellCode.getOemCode();
        StringBuilder sb = new StringBuilder();
        int dashCount = 0;
        for (char c : tbinStr.toCharArray()) {
            if (c == '-') {
                dashCount++;
                if (dashCount == 3) {
                    sb.append('^');
                }
                sb.append('^');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
