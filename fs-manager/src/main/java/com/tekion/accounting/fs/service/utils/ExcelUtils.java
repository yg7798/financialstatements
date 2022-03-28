package com.tekion.accounting.fs.service.utils;

import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.exportable.lib.model.EnhancedCsvFileData;
import com.tekion.core.exportable.lib.processors.ExcelWriter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

@UtilityClass
@Slf4j
public class ExcelUtils {
    public static void generateExcelHTTPResponseUsingStream(OutputStream outputStream, EnhancedCsvFileData data) {
        ExcelWriter.writeToResponseOutputUsingSXSSFV2(outputStream, data);
    }
    public static void validateExcelFile(File file) throws IOException {
        Workbook workbook = null;
        try{
            workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if(!rowIterator.hasNext()){
                throw new TBaseRuntimeException(FSError.uploadValidExcelFile);
            }

            rowIterator.next(); // skip header row

            if(!rowIterator.hasNext() ){
                throw new TBaseRuntimeException(FSError.uploadValidExcelFile);
            }

            Row row = rowIterator.next();
            if(row == null || row.getCell(0) == null || row.getCell(0).getCellType().equals(CellType.BLANK)){
                throw new TBaseRuntimeException(FSError.entryNumberCannotBeEmpty);
            }
        }catch (IOException ioException){
            log.error("validate excel sheet failed with error: ",ioException);
            throw new TBaseRuntimeException(FSError.ioError);
        }finally {
            assert workbook != null;
            workbook.close();
        }
    }
}
