package com.tekion.accounting.fs.service.externalService.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.poiji.bind.Poiji;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.dto.pclCodes.PclUpdateExcelDto;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.service.common.pdfPrinting.PDFPrintService;
import com.tekion.accounting.fs.service.common.pdfPrinting.dto.MediaResponse;
import com.tekion.accounting.fs.service.common.pdfPrinting.dto.MediaResponseEntity;
import com.tekion.accounting.fs.service.utils.ExcelUtils;
import com.tekion.core.excelGeneration.models.model.MediaUploadResponse;
import com.tekion.core.excelGeneration.models.utils.JsonUtil;
import com.tekion.core.exportable.lib.model.EnhancedCsvFileData;
import com.tekion.core.exportable.lib.processors.CSVDataCreator;
import com.tekion.core.service.internalauth.AbstractServiceClientFactory;
import com.tekion.core.utils.TCollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.tekion.accounting.fs.service.common.pdfPrinting.PDFPrintService.buildHeaders;

@Component
@Slf4j
@RequiredArgsConstructor
public class MediaInteractorService {
    @Autowired
    public AbstractServiceClientFactory abstractServiceClientFactory;
    @Autowired
    public PDFPrintService pdfPrintService;
    private final RestTemplate restTemplate;
    public static final String mediaUploadEndPoint= "/media/w/u/upload";

    public <T> MediaUploadResponse getMediaUploadResponse(List<T> rowToPrint, Class<T> reportLineClassName) {
        CSVDataCreator<T> csvDataCreator = new CSVDataCreator<>();
        EnhancedCsvFileData enhancedCSVForBeans  = csvDataCreator.createAndReturnEnhancedCSVForBeans(reportLineClassName, rowToPrint);
        FileOutputStream fileOutputStream = null;
        File excelDownload = null;
        try {

            String fileName = getReportFileName() + ".xlsx";
            excelDownload = File.createTempFile("excelFile" + "_" + UUID.randomUUID(), ".xlsx");
            fileOutputStream = new FileOutputStream(excelDownload);
            ExcelUtils.generateExcelHTTPResponseUsingStream(fileOutputStream,enhancedCSVForBeans);
            List<PclUpdateExcelDto> excelContent =  TCollectionUtils.nullSafeList(Poiji.fromExcel(excelDownload, PclUpdateExcelDto.class));
            return uploadFile(excelDownload, fileName, ExternalServiceConstant.MS_EXCEL);

        } catch (IOException e) {
            log.error("io exceptionOccurred : ",e);
        }
        finally {
            if (Objects.nonNull(excelDownload)) {
                excelDownload.delete();
            }
            if(Objects.nonNull(fileOutputStream)){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    log.error("failed to close output stream");
                }
            }
        }
        log.error("error occurred returning null");
        return null;
    }

    public MediaUploadResponse uploadFile(File file, String fileName, MediaType mediaType) throws IOException {
        Response response = null;
        try {
            try {
                String uploadMediaUrl = getUploadMediaUrl();
                response = ExternalCallUtils.uploadFileToMediaService(uploadMediaUrl, file,fileName, mediaType);
            } catch (Exception e) {
                log.error("error occurred while making externalCall : ", e);
            }
            if (Objects.isNull(response)) {
                log.error("upload to media failed.");
            }
            return parseResponse(response);
        }
        finally {
            if(Objects.nonNull(response)){
                response.close();
            }
        }
    }

    private MediaUploadResponse parseResponse(Response response) throws IOException {
        if (Objects.isNull(response)) {
            return null;
        }
        JsonNode jsonNode = JsonUtil.MAPPER.readTree(new ByteArrayInputStream(response.body().bytes()));

        MediaUploadResponse mediaUploadResponse = JsonUtil.MAPPER.treeToValue(jsonNode.at("/" + "data"), MediaUploadResponse.class);
        return mediaUploadResponse;
    }

    private String getUploadMediaUrl() {
        return abstractServiceClientFactory.getServiceBaseUrl(ExternalServiceConstant.MEDIA_SERVICE) + mediaUploadEndPoint;
    }

    private String getReportFileName(){
        return UUID.randomUUID().toString();
    }
}
