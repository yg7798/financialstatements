package com.tekion.accounting.fs.service.utils;

import com.tekion.core.exportable.lib.model.EnhancedCsvFileData;
import com.tekion.core.exportable.lib.processors.ExcelWriter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;

@UtilityClass
@Slf4j
public class ExcelUtils {
    public static void generateExcelHTTPResponseUsingStream(OutputStream outputStream, EnhancedCsvFileData data) {
        ExcelWriter.writeToResponseOutputUsingSXSSFV2(outputStream, data);
    }
}
