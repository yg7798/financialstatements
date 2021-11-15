package com.tekion.accounting.fs.common.pdfPrinting;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.pdfPrinting.dto.*;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.admin.beans.Department;
import com.tekion.admin.beans.beansdto.printerDto.Options;
import com.tekion.admin.beans.printer.ModuleMapping;
import com.tekion.admin.beans.printer.PrinterMapping;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.clients.preference.client.TekionResponse;
import com.tekion.core.beans.TResponse;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.serverconfig.beans.ServerType;
import com.tekion.core.serverconfig.service.ServerConfigServiceImpl;
import com.tekion.core.utils.*;
import com.tekion.cs.beans.ServerConfigFlat;
import com.tekion.printer.beans.dto.BulkPrinterDetailsResponseDTO;
import com.tekion.printer.beans.dto.PrintRequestV2;
import com.tekion.printer.beans.dto.bulk.BulkPrintResponse;
import com.tekion.printer.beans.dto.mappings.PrinterMappingRequestDTO;
import com.tekion.printer.beans.enums.FormModule;
import com.tekion.printer.beans.tcp.PrintDetails;
import com.tekion.printer.beans.tcp.TCPPrintResponse;
import com.tekion.printerclient.PrinterClient;
import com.tekion.printerclientv2.PrinterClientV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tekion.core.utils.TGlobalConstants.NO_DEALER_ID;
import static com.tekion.core.utils.TGlobalConstants.NO_TENANT_ID;
import static com.tekion.tekionconstant.permission.Department.ACCOUNTING;

@Service
@Slf4j
@RequiredArgsConstructor
public class PDFPrintService {
    private final RestTemplate restTemplate;
    private final PreferenceClient preferenceClient;
    private final PrinterClient printerClient;
    //private final APSetupService apSetupService;
    private final PrinterClientV2 printerClientV2;

    private final int BATCH_SIZE_FOR_FETCHING_MEDIA_URL = 500;
    private final int INVALID_PAGE_START = -1;
    private final int INVALID_PAGE_END = -1;
    private final int DEFAULT_CHECK_PAGE_NO = 1;
    private final int NO_OF_PERFORATED_PAGES = 1;
    private final String PAGE_RANGE_PRINT = "page_range_print";
    private final String DEFAULT_PAPER_SIZE_LETTER = "Letter";
    private final String AR_STMT_PRINT_RESPONSE_LOG_LINE = "AR Statement Print";

    //todo retry count =3 and check what is wrong with content type
    public boolean exportPdf(PdfRequest pdfRequest, UserContext context){
        String logMsg=  " callbackUrl: " + pdfRequest.getCallbackUrl () + " targetUrl : " +pdfRequest.getTargetUrl ();
        try{
            HttpEntity<PdfRequest> httpEntity = new HttpEntity <> ( pdfRequest, buildHeaders() );
            log.info("{} : requestId : {} , pdf request {}", TConstants.PDF_PRINT_SERVICE, pdfRequest.getId(), pdfRequest);
            ResponseEntity<ExportPdfResponseEntity> responseEntity = restTemplate
                    .exchange ( exportPdfUrl(), HttpMethod.POST, httpEntity, ExportPdfResponseEntity.class );
            log.info("{} : requestId : {} , pdf response {}",TConstants.PDF_PRINT_SERVICE, pdfRequest.getId(), responseEntity);
            return responseEntity.getStatusCodeValue () == 200;
        } catch (Exception e){
            log.error ("{} : unable to export pdf for url: {} {} {}" ,TConstants.PDF_PRINT_SERVICE, exportPdfUrl(), logMsg ,e );
            return false;
        }
    }

    public boolean exportBulkPdf(BulkPdfRequest pdfRequest, UserContext context){
        String logMsg=  " requestId : " +pdfRequest.getId () + " callbackUrl: " + pdfRequest.getCallbackUrl ();
        try{
            HttpHeaders headers = buildHeaders();
            headers.set(TGlobalConstants.TENANT_NAME_KEY, UserContextUtils.getTenantId(context.getDseUserContext ().getTenantName ()));
            String exportUrl = exportBulkPdfUrl();

            HttpEntity<BulkPdfRequest> httpEntity = new HttpEntity <> ( pdfRequest, headers );
            log.info("{} : pdf request  {} export url {}",TConstants.PDF_PRINT_SERVICE,pdfRequest, exportUrl);

            ResponseEntity<ExportPdfResponseEntity> responseEntity = restTemplate
                    .exchange ( exportUrl, HttpMethod.POST, httpEntity, ExportPdfResponseEntity.class );

            log.info("{} : pdf response {}",TConstants.PDF_PRINT_SERVICE, responseEntity);
            log.info ( "{} : export pdf request successful , {}",TConstants.PDF_PRINT_SERVICE, logMsg );
            return responseEntity.getStatusCodeValue () == 200;
        } catch (HttpStatusCodeException e){
            log.error ( "{} : unable to export pdf for url: {}, {}, {}" ,TConstants.PDF_PRINT_SERVICE, exportBulkPdfUrl(),logMsg , e );
            return false;
        } catch (Exception e){
            log.error ( "{} : unable to export pdf for url: {}, {}, {}" ,TConstants.PDF_PRINT_SERVICE , exportBulkPdfUrl(),logMsg , e );
            return false;
        }
    }


    public Object exportBulkPdfWithResponse(BulkPdfRequest pdfRequest, UserContext context){
        String logMsg=  " requestId : " +pdfRequest.getId () + " callbackUrl: " + pdfRequest.getCallbackUrl ();
        try{
            HttpHeaders headers = buildHeaders();
            String exportUrl = exportBulkPdfUrl();

            HttpEntity<BulkPdfRequest> httpEntity = new HttpEntity <> ( pdfRequest, headers );
            log.info("{} : pdf request  {} export url {}",TConstants.PDF_PRINT_SERVICE,pdfRequest, exportUrl);

            ResponseEntity<Object> responseEntity = restTemplate
                    .exchange ( exportUrl, HttpMethod.POST, httpEntity, Object.class );

            log.info("{} : pdf response {}",TConstants.PDF_PRINT_SERVICE, responseEntity);
            return responseEntity.getBody();
        } catch (Exception e){
            log.error ( "{} : unable to export pdf for url: {}, {}, {}" ,TConstants.PDF_PRINT_SERVICE , exportBulkPdfUrl(),logMsg , e );
            return null;
        }
    }

    public List<MediaResponse> requestMediaServiceForSignedUrl(List <MediaItem> mediaItems) {
        return requestMediaServiceForSignedUrl(mediaItems, UserContextProvider.getContext());
    }

    public List<MediaResponse> requestMediaServiceForSignedUrl(List <MediaItem> mediaItems, UserContext context) {
        List<String> mediaIds = TCollectionUtils.transformToList ( mediaItems,MediaItem::getId );
        HttpEntity<List<String>> httpEntity = new HttpEntity <> ( mediaIds, buildHeaders () );
        try{
            ResponseEntity<MediaResponseEntity> responseEntity = restTemplate
                .exchange ( fetchMediaUrl(), HttpMethod.POST, httpEntity, MediaResponseEntity.class );
            if(responseEntity.getStatusCodeValue () == 200 && responseEntity.getBody ()!= null ){
                List <MediaResponse> medias = responseEntity.getBody ().getData ();
                log.debug ("{} : media request successful for mediaId: {}",TConstants.PDF_PRINT_SERVICE, mediaIds);
                return medias;
            }
        } catch (Exception e){
            log.error("{} : media service request failed for mediaIds:{} ",TConstants.PDF_PRINT_SERVICE, mediaIds, e);
            throw new TBaseRuntimeException(FSError.mediaServiceRequestFailed);
        }
        return null;
    }

    public List<MediaResponse> requestMediaServiceForBulkSignedUrl(List <BulkMediaItem> mediaItems) {
        List<String> mediaIds = TCollectionUtils.transformToList ( mediaItems,BulkMediaItem::getMediaId );
        if(TCollectionUtils.isEmpty(mediaIds)) {
            return Lists.newArrayList();
        }
        List<List<String>> partitionedMediaId = Lists.partition(mediaIds, BATCH_SIZE_FOR_FETCHING_MEDIA_URL);
        List<MediaResponse> combinedResponse = Lists.newArrayList();
        for(List<String> mediaIdsList : partitionedMediaId) {
            HttpEntity<List<String>> httpEntity = new HttpEntity <> ( mediaIdsList, buildHeaders () );
            try{
                log.info ("{} : presigned URLs requested: {}",TConstants.PDF_PRINT_SERVICE, mediaIdsList);
                ResponseEntity<MediaResponseEntity> responseEntity = restTemplate
                        .exchange ( fetchMediaUrl(), HttpMethod.POST, httpEntity, MediaResponseEntity.class );
                if(responseEntity.getStatusCodeValue () == 200 && responseEntity.getBody ()!= null ){
                    List <MediaResponse> medias = responseEntity.getBody ().getData ();
                    log.debug ( "{} : media request successful for mediaId: {}",TConstants.PDF_PRINT_SERVICE, mediaIdsList);
                    combinedResponse.addAll(TCollectionUtils.nullSafeCollection(medias));
                }
            } catch (Exception e){
                log.error("{} : media service request failed for mediaIds : {}",TConstants.PDF_PRINT_SERVICE, mediaIdsList);
                throw new RestClientException ("media service request failed",e);
            }
        }
        return combinedResponse;
    }


//    public boolean printPdf(Department department, FormModule module, String url, UserContext context){
//        return printPdfWithPageRange(department, module, url, context, -1,TConstants.DEFAULT_COPY_NUM);
//    }

//    public boolean printPdfWithPageRange(Department department, FormModule module, String url, UserContext context, int pageCount, int noOfCopies){
//        log.info("{} : formModule:{} {} 1 pageCount:{} NoOfCopies: {} {}",TConstants.PDF_PRINT_SERVICE, module.name(), PAGE_RANGE_PRINT, pageCount,noOfCopies, UserContextProvider.getDSETenantName());
//        try{
//            boolean printed;
//            if(module.equals(FormModule.CHECK_PRINT) || module.equals(FormModule.DEPOSIT_SLIP)){
//                PrintDetails printDetail = PrintDetails.builder()
//                        .copies(TConstants.DEFAULT_COPY_NUM)
//                        .url(url)
//                        .pageStart(DEFAULT_CHECK_PAGE_NO)
//                        .pageEnd(DEFAULT_CHECK_PAGE_NO)
//                        .build();
//
//                PrintRequestV2 printRequest = PrintRequestV2.builder()
//                        .department(com.tekion.tekionconstant.permission.Department.ACCOUNTING)
//                        .formModule(module)
//                        .userId(UserContextProvider.getCurrentUserId())
//                        .options(getOptions(module))
//                        .printDetails(Collections.singletonList(printDetail))
//                        .build();
//
//                log.info("{} : print request : {}",TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(printRequest));
//
//                CompletableFuture<TResponse<Map<String, TCPPrintResponse>>> printResponse = printerClient.printV3(TRequestUtils.userCallHeaderMap(), printRequest);
//
//                try {
//                    printed = printResponse.get().getStatus().equalsIgnoreCase("success");
//                } catch (Exception e) {
//                    log.error("{} : print response failed",TConstants.PDF_PRINT_SERVICE, e);
//                    printed = false;
//                }
//                log.info("{} : formModule:{} {} 2 pageCount:{} printed: {} ",TConstants.PDF_PRINT_SERVICE, module.name(),  PAGE_RANGE_PRINT, pageCount, printed);
//
//                if(printed && (pageCount == ALL_PAGES || pageCount > 1)){
//
//                    int pageStart = pageCount == ALL_PAGES ? ALL_PAGES : 2;
//                    log.info("{} : {} : 3 pageCount:{}, pageStart {} ",TConstants.PDF_PRINT_SERVICE, PAGE_RANGE_PRINT, pageCount, pageStart);
//
//
//                    PrintDetails printDetailForPagesOnCheckCopyPrinter = PrintDetails.builder()
//                            .copies(TConstants.DEFAULT_COPY_NUM)
//                            .url(url)
//                            .pageStart(pageStart)
//                            .pageEnd(pageCount)
//                            .build();
//
//                    PrintRequestV2 printingRequest = PrintRequestV2.builder()
//                            .department(com.tekion.tekionconstant.permission.Department.ACCOUNTING)
//                            .formModule(CHECK_COPY)
//                            .userId(UserContextProvider.getCurrentUserId())
//                            .options(getOptions(module))
//                            .printDetails(Collections.singletonList(printDetailForPagesOnCheckCopyPrinter))
//                            .build();
//
//                    log.info("{} : print request : {}",TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(printRequest));
//
//                    CompletableFuture<TResponse<Map<String, TCPPrintResponse>>> printCopyResponse = printerClient.printV3(TRequestUtils.userCallHeaderMap(), printingRequest);
//                    try {
//                        printed = printCopyResponse.get().getStatus().equalsIgnoreCase("success");
//                    } catch(HystrixRuntimeException e){
//                        log.error("print response timed out : {} for {}  {}", TConstants.PDF_PRINT_SERVICE, "printPdfWithPageRange", e);
//                    }
//                    catch (Exception e) {
//                        log.error("{} : print response failed",TConstants.PDF_PRINT_SERVICE, e);
//                        printed = false;
//                    }
//
//                    log.info("{} : {} : 4 pageCount:{} printed: {} ",TConstants.PDF_PRINT_SERVICE, PAGE_RANGE_PRINT, pageCount, printed);
//                }
//            }else{
//                log.info("{} : formModule:{} {} 5 pageCount:{} ",TConstants.PDF_PRINT_SERVICE, module.name(),  PAGE_RANGE_PRINT, pageCount);
//
//
//                PrintDetails printDetailsForCheckCopy = PrintDetails.builder()
//                        .copies(noOfCopies)
//                        .url(url)
//                        .pageStart(INVALID_PAGE_START)
//                        .pageEnd(INVALID_PAGE_END)
//                        .build();
//
//                PrintRequestV2 printRequest = PrintRequestV2.builder()
//                        .department(com.tekion.tekionconstant.permission.Department.ACCOUNTING)
//                        .formModule(com.tekion.printer.beans.enums.FormModule.fromValue(module.toString()))
//                        .userId(UserContextProvider.getCurrentUserId())
//                        .options(FormModule.FORM_GENERATION.equals(module) ? getOptionsForFormGenerationModule(module) : getOptions(module))
//                        .printDetails(Collections.singletonList(printDetailsForCheckCopy))
//                        .build();
//
//                log.info("{} : print request : {}",TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(printRequest));
//
//                CompletableFuture<TResponse<Map<String, TCPPrintResponse>>> printResponse = printerClient.printV3(TRequestUtils.userCallHeaderMap(), printRequest);
//                try {
//                    printed = printResponse.get().getStatus().equalsIgnoreCase("success");
//                } catch (Exception e) {
//                    log.error("{} : print response failed",TConstants.PDF_PRINT_SERVICE, e);
//                    printed = false;
//                }
//            }
//
//            if(printed){
//                log.info ( "{} : print successful for pdf url {}, department: {}, module: {}",TConstants.PDF_PRINT_SERVICE, url, department.name (),module.name () );
//                return true;
//            } else {
//                log.error ( "{} : print request to printer client failed",TConstants.PDF_PRINT_SERVICE);
//                return false;
//            }
//        } catch (Exception e){
//            log.error ( "print failed for pdf url {}, department: {}, module: {}, ex : {}",TConstants.PDF_PRINT_SERVICE, url, department.name (),module.name ()  ,e);
//            return false;
//        }
//    }

//    public boolean printPdfSingle(Department department, FormModule module, String url, UserContext context){
//        try{
//            boolean printed;
//            PrintDetails printDetail = PrintDetails.builder()
//                    .copies(TConstants.DEFAULT_COPY_NUM)
//                    .url(url)
//                    .pageStart(INVALID_PAGE_START)
//                    .pageEnd(INVALID_PAGE_END)
//                    .build();
//
//            PrintRequestV2 printRequest = PrintRequestV2.builder()
//                    .department(com.tekion.tekionconstant.permission.Department.ACCOUNTING)
//                    .formModule(com.tekion.printer.beans.enums.FormModule.fromValue(module.toString()))
//                    .userId(UserContextProvider.getCurrentUserId())
//                    .options(getOptions(module))
//                    .printDetails(Collections.singletonList(printDetail))
//                    .build();
//
//            log.info("{} : print request : {}",TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(printRequest));
//
//            CompletableFuture<TResponse<Map<String, TCPPrintResponse>>> printResponse = printerClient.printV3(TRequestUtils.userCallHeaderMap(), printRequest);
//
//            try {
//                printed = printResponse.get().getStatus().equalsIgnoreCase("success");
//            } catch (Exception e) {
//                log.error("{} : print response failed",TConstants.PDF_PRINT_SERVICE, e);
//                printed = false;
//            }
//
//            if(printed){
//                log.info ( "{} : print successful for pdf url {}, department: {}, module: {}",TConstants.PDF_PRINT_SERVICE, url, department.name (),module.name () );
//                return true;
//            } else {
//                log.error ( "{} : print request to printer client failed",TConstants.PDF_PRINT_SERVICE);
//                return false;
//            }
//        } catch (Exception e){
//            log.error ( "print failed for pdf url {}, department: {}, module: {}, ex : ", url, department.name (),module.name ()  ,e);
//            return false;
//        }
//    }

//    public boolean printArStatementV2(com.tekion.tekionconstant.permission.Department department, Map<String, Integer> urlsToPageNumberMapping, UserContext context, int noOfCopies) {
//        Set<BulkPrinterDetailsResponseDTO> printerMappings = getPrinterMappingInfo(context, Sets.newHashSet(AR_CASH_RECEIPT, CASH_RECEIPT));
//        boolean differentPrinterMapping = checkForMultiplePrinterMapping(printerMappings);
//        boolean isPrintSuccess;
//        if(differentPrinterMapping) {
//            isPrintSuccess = printArStatementOnDiffPrinter(department, urlsToPageNumberMapping, context, noOfCopies);
//        } else {
//            if(printerMappings.size() == 1) {
//                isPrintSuccess = printArStatementOnSinglePrinter(department, printerMappings.iterator().next().getModule(), urlsToPageNumberMapping, context, -1, noOfCopies);
//            } else {
//                isPrintSuccess = printArStatementOnSinglePrinter(department, CASH_RECEIPT, urlsToPageNumberMapping, context, -1, noOfCopies);
//            }
//        }
//        log.info("Ar statement printing status {}", isPrintSuccess);
//        return isPrintSuccess;
//    }

//    private boolean printArStatementOnSinglePrinter(com.tekion.tekionconstant.permission.Department department, FormModule formModule, Map<String, Integer> urlsToPageNumberMapping, UserContext context, int i, int noOfCopies) {
//        log.info("Ar statement printing with same printer mappings");
//        try {
//            boolean printed;
//            log.info("{} : NoOfCopies: {}", TConstants.PDF_PRINT_SERVICE, noOfCopies);
//            List<PrintDetails> printingPageDetails = new ArrayList<>();
//            for (String url : urlsToPageNumberMapping.keySet()) {
//                Integer noOfPages = urlsToPageNumberMapping.get(url);
//                PrintDetails pagePrintDetail = PrintDetails.builder()
//                        .copies(noOfCopies)
//                        .url(url)
//                        .pageStart(1)
//                        .pageEnd(noOfPages)
//                        .build();
//
//                printingPageDetails.add(pagePrintDetail);
//            }
//            log.info("{} : Print request for printing {}", TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(printingPageDetails));
//            PrintOptions options = new PrintOptions();
//            options.setPaperSize(DEFAULT_PAPER_SIZE_LETTER);
//
//            BulkPrintRequestV2 bulkPrintRequest = new BulkPrintRequestV2();
//            bulkPrintRequest.setDepartment(department);
//            bulkPrintRequest.setModule(FormModule.AR_CASH_RECEIPT);
//            bulkPrintRequest.setOptions(options);
//            bulkPrintRequest.setUserId(UserContextProvider.getCurrentUserId());
//            bulkPrintRequest.setPrintDetails(printingPageDetails);
//
//            log.info("{} : printing Request payload {}", TConstants.PDF_PRINT_SERVICE, bulkPrintRequest);
//
//            Map<String, BulkPrintResponse> printResponse = printerClientV2.bulkPrint(TRequestUtils.userCallHeaderMap(), bulkPrintRequest).getData();
//            try {
//                printed = getPrintStatusFromBulkPrintResponse(printResponse, AR_STMT_PRINT_RESPONSE_LOG_LINE);
//                log.info("{} : printing response {}", TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(printResponse));
//            } catch (Exception e) {
//                log.error("{} : print response failed", TConstants.PDF_PRINT_SERVICE, e);
//                printed = false;
//            }
//
//            if (printed) {
//                log.info("{} BulkPrint request to printerClient successful", TConstants.PDF_PRINT_SERVICE);
//                return true;
//            } else {
//                log.error("{} : print request to printer client failed", TConstants.PDF_PRINT_SERVICE);
//                return false;
//            }
//        } catch (Exception e) {
//            log.error("{} : print failed for BulkPrint for urls {}, department: {}, ex : ", TConstants.PDF_PRINT_SERVICE, urlsToPageNumberMapping, department.name(), e);
//            return false;
//        }
//    }

    private boolean getPrintStatusFromBulkPrintResponse(Map<String, BulkPrintResponse> printResponse, String logMessage) {
        log.info("{} PrintResponse from printerClient :- {}", logMessage, JsonUtil.toJson(printResponse));
        for(String printResKey : printResponse.keySet()) {
            if(Objects.nonNull(printResponse.get(printResKey)) &&
                    TCollectionUtils.isNotEmpty(printResponse.get(printResKey).getFailedJobs())) {
                return false;
            }
        }
        return true;
    }

//    //return true if mapped to different printer
//    public boolean checkForMultiplePrinterMapping(Set<BulkPrinterDetailsResponseDTO> printerDetailsResponse) {
//        if(TCollectionUtils.isEmpty(printerDetailsResponse)) {
//            throw new TBaseRuntimeException("No printer mappings found!");
//        }
//        BulkPrinterDetailsResponseDTO cashReceiptModulePrinterInfo = null;
//        BulkPrinterDetailsResponseDTO arCashReceiptModulePrinterInfo = null;
//        for(BulkPrinterDetailsResponseDTO bulkPrinterDetailsResponseDTO : printerDetailsResponse) {
//            if(FormModule.CASH_RECEIPT == bulkPrinterDetailsResponseDTO.getModule()) {
//                cashReceiptModulePrinterInfo = bulkPrinterDetailsResponseDTO;
//            } else if (FormModule.AR_CASH_RECEIPT == bulkPrinterDetailsResponseDTO.getModule()) {
//                arCashReceiptModulePrinterInfo = bulkPrinterDetailsResponseDTO;
//            }
//        }
//        if(printerDetailsResponse.size() == 1) {
//            log.info("Only one printer mapping found for module : {}", printerDetailsResponse.iterator().next().getModule());
//            return false;
//        }
//        if(arCashReceiptModulePrinterInfo != null && cashReceiptModulePrinterInfo != null) {
//            Set<com.tekion.printer.beans.PrinterDetails> arCashReceiptPrinterDetails = TCollectionUtils.transformToSet(arCashReceiptModulePrinterInfo.getPrinterDetails());
//            Set<com.tekion.printer.beans.PrinterDetails> cashReceiptPrinterDetails = TCollectionUtils.transformToSet(cashReceiptModulePrinterInfo.getPrinterDetails());
//            Set<String> macIdsForCashReceiptPrinter = cashReceiptPrinterDetails.stream().map(PrinterDetails::getMacId).collect(Collectors.toSet());
//            for(com.tekion.printer.beans.PrinterDetails printerDetails : arCashReceiptPrinterDetails) {
//                if(macIdsForCashReceiptPrinter.contains(printerDetails.getMacId())) {
//                    log.info("Common printer found with printerId : {}", printerDetails.getPrinterId());
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    /**
     * Returns printer mapping for given formModules and department for that user
     * If no mapping found for that user it will return the printer mapped under default for given formModules and department
     * @param context
     * @param formModules
     * @return
     */
    private Set<BulkPrinterDetailsResponseDTO> getPrinterMappingInfo(UserContext context, Set<FormModule> formModules) {
        Set<PrinterMappingRequestDTO> printerInfoRequest = Sets.newHashSet();
        for(FormModule formModule : formModules) {
            PrinterMappingRequestDTO printerMappingRequestDto = new PrinterMappingRequestDTO(ACCOUNTING, formModule, context.getUserId());
            printerInfoRequest.add(printerMappingRequestDto);
        }
        log.info("ACCOUNTING: PrinterMapping info Request payload {}", JsonUtil.toJson(printerInfoRequest));
        TResponse<Set<BulkPrinterDetailsResponseDTO>> printerDetailsResponse = printerClientV2.fetchPrinterDetailsBulk(TRequestUtils.userCallHeaderMap(), printerInfoRequest);
        log.info("ACCOUNTING: PrinterMapping info Response {}", JsonUtil.toJson(printerDetailsResponse));
        if(TCollectionUtils.isEmpty(printerDetailsResponse.getData())) {
            throw new TBaseRuntimeException("No printerFound!");
        }
        return printerDetailsResponse.getData();
    }

//    public boolean printArStatementOnDiffPrinter(com.tekion.tekionconstant.permission.Department department, Map<String, Integer> urlsToPageNumberMapping, UserContext context, int noOfCopies) {
//        log.info("Ar statement printing with different printer mappings");
//        try {
//            boolean printed;
//            log.info("{} : NoOfCopies: {}",TConstants.PDF_PRINT_SERVICE, noOfCopies);
//            List<PrintDetails> printDetailsPage2Onwards = new ArrayList<>();
//            List<PrintDetails> firstPagePrintDetails = new ArrayList<>();
//            for(String url : urlsToPageNumberMapping.keySet()) {
//                Integer noOfPages = urlsToPageNumberMapping.get(url);
//                PrintDetails singleFirstPagePrintDetail = PrintDetails.builder()
//                        .copies(noOfCopies)
//                        .url(url)
//                        .pageStart(1)
//                        .pageEnd(NO_OF_PERFORATED_PAGES)
//                        .build();
//
//                firstPagePrintDetails.add(singleFirstPagePrintDetail);
//
//                if(noOfPages > 1) {
//                    PrintDetails singleSecondPageOnwardsPrintDetail = PrintDetails.builder()
//                            .copies(noOfCopies)
//                            .url(url)
//                            .pageStart(NO_OF_PERFORATED_PAGES + 1)
//                            .pageEnd(noOfPages)
//                            .build();
//                    printDetailsPage2Onwards.add(singleSecondPageOnwardsPrintDetail);
//                }
//            }
//            log.info("{} : Print request for first page {}", TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(firstPagePrintDetails));
//            log.info("{} : Print request for second page onwards {}", TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(printDetailsPage2Onwards));
//            PrintOptions options = new PrintOptions();
//            options.setPaperSize(DEFAULT_PAPER_SIZE_LETTER);
//
//            BulkPrintRequestV2 firstPageBulkPrintRequest = new BulkPrintRequestV2();
//            firstPageBulkPrintRequest.setDepartment(department);
//            firstPageBulkPrintRequest.setModule(FormModule.AR_CASH_RECEIPT);
//            firstPageBulkPrintRequest.setOptions(options);
//            firstPageBulkPrintRequest.setUserId(UserContextProvider.getCurrentUserId());
//            firstPageBulkPrintRequest.setPrintDetails(firstPagePrintDetails);
//
//            boolean printStatusForPage2Onwards = false;
//            BulkPrintRequestV2 secondPageOnwardsBulkPrintRequest = null;
//            if(TCollectionUtils.isEmpty(printDetailsPage2Onwards)) {
//                printStatusForPage2Onwards = true;
//                log.info("{}: No page 2 request", TConstants.PDF_PRINT_SERVICE);
//            } else {
//                secondPageOnwardsBulkPrintRequest =  new BulkPrintRequestV2();
//                secondPageOnwardsBulkPrintRequest.setDepartment(department);
//                secondPageOnwardsBulkPrintRequest.setModule(CASH_RECEIPT);
//                secondPageOnwardsBulkPrintRequest.setOptions(options);
//                secondPageOnwardsBulkPrintRequest.setUserId(UserContextProvider.getCurrentUserId());
//                secondPageOnwardsBulkPrintRequest.setPrintDetails(printDetailsPage2Onwards);
//            }
//            log.info("{} : firstPage printing Request payload {}", TConstants.PDF_PRINT_SERVICE, firstPageBulkPrintRequest);
//            log.info("{} : secondPage onwards printing Request payload {}", TConstants.PDF_PRINT_SERVICE, secondPageOnwardsBulkPrintRequest);
//
//            Map<String, BulkPrintResponse> firstPagePrintResponse = printerClientV2.bulkPrint(TRequestUtils.userCallHeaderMap(), firstPageBulkPrintRequest).getData();
//            boolean firstPagePrintStatus = getPrintStatusFromBulkPrintResponse(firstPagePrintResponse, AR_STMT_PRINT_RESPONSE_LOG_LINE);
//            if(!printStatusForPage2Onwards) {
//                Map<String, BulkPrintResponse> secondPageOnwardsPrintResponse = printerClientV2.bulkPrint(TRequestUtils.userCallHeaderMap(), secondPageOnwardsBulkPrintRequest).getData();
//                if(Objects.isNull(secondPageOnwardsPrintResponse)) {
//                    printStatusForPage2Onwards = false;
//                } else {
//                    printStatusForPage2Onwards = getPrintStatusFromBulkPrintResponse(secondPageOnwardsPrintResponse, AR_STMT_PRINT_RESPONSE_LOG_LINE);
//                }
//                log.info("{} : second page onwards printing response {}", TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(secondPageOnwardsPrintResponse));
//            }
//
//            try {
//                printed = firstPagePrintStatus && printStatusForPage2Onwards;
//                log.info("{} : first page printing response {}", TConstants.PDF_PRINT_SERVICE, printed);
//                //print response for 1st and 2nd
//            } catch (Exception e) {
//                log.error("{} : print response failed",TConstants.PDF_PRINT_SERVICE, e);
//                printed = false;
//            }
//
//            if (printed) {
//                log.info("{} BulkPrint request to printerClient successful", TConstants.PDF_PRINT_SERVICE);
//                return true;
//            } else {
//                log.error("{} : print request to printer client failed",TConstants.PDF_PRINT_SERVICE);
//                return false;
//            }
//        } catch (Exception e){
//            log.error ( "{} : print failed for BulkPrint for urls {}, department: {}, ex : ",TConstants.PDF_PRINT_SERVICE, urlsToPageNumberMapping, department.name(), e);
//            return false;
//        }
//    }

    public boolean printBulkPdf(Department department, FormModule module, List<String> urls, UserContext context, int noOfCopies) {
        try {
            boolean printed;
            log.info("{} : formModule: {} NoOfCopies: {}",TConstants.PDF_PRINT_SERVICE, module, noOfCopies);

            List<PrintDetails> printDetailsList = new ArrayList<>();
            for (String url : urls) {
                PrintDetails printDetail = PrintDetails.builder()
                        .copies(noOfCopies)
                        .url(url)
                        .pageStart(INVALID_PAGE_START)
                        .pageEnd(INVALID_PAGE_END)
                        .build();
                printDetailsList.add(printDetail);
            }

            PrintRequestV2 printRequest = PrintRequestV2.builder()
                    .department(com.tekion.tekionconstant.permission.Department.ACCOUNTING)
                    .formModule(com.tekion.printer.beans.enums.FormModule.fromValue(module.toString()))
                    .userId(UserContextProvider.getCurrentUserId())
                    //.options(getOptions(module))
                    .printDetails(printDetailsList)
                    .build();

            log.info("{} : print request : {}",TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(printRequest));

            CompletableFuture<TResponse<Map<String, TCPPrintResponse>>> printResponse = printerClient.printV3(TRequestUtils.userCallHeaderMap(), printRequest);

            try {
                printed = printResponse.get().getStatus().equalsIgnoreCase("success");
            } catch (Exception e) {
                log.error("{} : print response failed",TConstants.PDF_PRINT_SERVICE, e);
                printed = false;
            }

            if (printed) {
                log.info("{} BulkPrint request to printerClient successful", TConstants.PDF_PRINT_SERVICE);
                return true;
            } else {
                log.error("{} : print request to printer client failed",TConstants.PDF_PRINT_SERVICE);
                return false;
            }
        } catch (Exception e){
            log.error ( "{} : print failed for BulkPrint for urls {}, department: {}, module: {}, ex : ",TConstants.PDF_PRINT_SERVICE, urls, department.name (),module.name ()  ,e);
            return false;
        }
    }

    public boolean printBulkPdfWithPageRange(FormModule formModule, List<BulkPrintPdfItem> printItems, UserContext context){

        try {
            boolean printed;

            log.info("printBulkPdf: {} BulkPrintPdfItems: {} formModule: {} PrintItems: {}",
                    TConstants.PDF_PRINT_SERVICE, printItems, formModule, JsonUtil.toJson(printItems));

            List<PrintDetails> printDetailsList = new ArrayList<>();
            for (BulkPrintPdfItem bulkPrintPdfItem : printItems) {
                PrintDetails printDetail = PrintDetails.builder()
                        .copies(bulkPrintPdfItem.getCopies())
                        .url(bulkPrintPdfItem.getUrl())
                        .pageStart(bulkPrintPdfItem.getPageStart().intValue())
                        .pageEnd(bulkPrintPdfItem.getPageEnd().intValue())
                        .duplex(bulkPrintPdfItem.isDuplex())
                        .build();
                printDetailsList.add(printDetail);
            }

            PrintRequestV2 printRequest = PrintRequestV2.builder()
                    .department(com.tekion.tekionconstant.permission.Department.ACCOUNTING)
                    .formModule(com.tekion.printer.beans.enums.FormModule.fromValue(formModule.toString()))
                    .userId(UserContextProvider.getCurrentUserId())
                    //.options(getOptions(formModule))
                    .printDetails(printDetailsList)
                    .build();

            log.info("{} : print request : {}",TConstants.PDF_PRINT_SERVICE, JsonUtil.toJson(printRequest));

            CompletableFuture<TResponse<Map<String, TCPPrintResponse>>> printResponse = printerClient.printV3(TRequestUtils.userCallHeaderMap(), printRequest);

            try {
                printed = printResponse.get().getStatus().equalsIgnoreCase("success");
            } catch (Exception e) {
                log.error("{} : print response failed",TConstants.PDF_PRINT_SERVICE, e);
                printed = false;
            }

            if (printed) {
                log.info("{} BulkPrint response from printerClient: {}", TConstants.PDF_PRINT_SERVICE, printResponse.get().getStatus());
                return true;
            } else {
                log.error("{} : print request to printer client failed",TConstants.PDF_PRINT_SERVICE);
                return false;
            }
        } catch (Exception e){
            log.error ( "{} : print failed for BulkPrint department: {}, module: {}, ex : ",TConstants.PDF_PRINT_SERVICE, Department.ACCOUNTING.name (),com.tekion.printer.beans.enums.FormModule.fromValue(formModule.toString()).name()  ,e);
            return false;
        }
    }

    public String getPrinterIdByDepartment(Department department, FormModule moduleName) {
        try{
            TekionResponse<List <PrinterMapping>> mappingsByDepartment = preferenceClient.getMappingsByDepartment ( department );
            List <PrinterMapping> printerMappings = mappingsByDepartment.getData ( );
            LinkedHashSet <ModuleMapping> moduleMappings = printerMappings.get ( 0 ).getModuleMappings ( );
            String printerId = printerMappings.get ( 0 ).getDefaultPrinter ().getMacId ();
            if(moduleName!=null){
                for ( ModuleMapping moduleMapping:moduleMappings ) {
                    if(moduleMapping.getModuleName ().equals (moduleName  )){
                        printerId= moduleMapping.getDefaultPrinter ().getMacId ();
                        break;
                    }
                }
            }
            return transformIdToCorrectFormat(printerId);
        } catch(Exception e){
            log.error ( "{} : unable to get printer details for department: {}",TConstants.PDF_PRINT_SERVICE, Department.ACCOUNTING.name (),e );
            return null;
        }
    }

    private String transformIdToCorrectFormat(String printerId) {
        Pattern p = Pattern.compile("(.{" + 2 + "})", Pattern.DOTALL);
        Matcher m = p.matcher(printerId);
        String reqPrinterId = m.replaceAll ( "$1" + ":" );
        if(reqPrinterId.substring(reqPrinterId.length() - 1).equals ( ":" )){
            return reqPrinterId.substring(0, reqPrinterId.length() - 1);
        } else {
            return reqPrinterId;
        }
    }

    private Options generateOptions(FormModule module) {
        return generateOptions(module, -1,  INVALID_PAGE_END,TConstants.DEFAULT_COPY_NUM);
    }


    private Options generateOptions(FormModule module, int pageStart, int pageEnd, int noOfCopies) {
        Options options = new Options();
        options.setCopies(noOfCopies);
        options.setPageStart(pageStart);
        if(pageStart != -1 ) options.setPageEnd(pageEnd);

        switch (module){
            case CHECK_PRINT:
                options.setPaperSize ( "Letter" );
                break;

            default:
                options.setPaperSize ( "Letter" );
                break;
        }
        return options;
    }

//    private PrinterOptions getOptions(FormModule module) {
//        PrinterOptions options = new PrinterOptions();
//        APSetup apSetup = apSetupService.getApSetup();
//        if(Objects.nonNull(apSetup.getCheckPrintSetting()) && Objects.nonNull(apSetup.getCheckPrintSetting().getScalePercent())
//                && (module.equals(FormModule.CHECK_PRINT) || module.equals(FormModule.CHECK_COPY))) {
//            options.setScalePercent(apSetup.getCheckPrintSetting().getScalePercent());
//            options.setPageFit(false);
//        }
//
//        options.setPaperSize(DEFAULT_PAPER_SIZE_LETTER);
//        return options;
//    }

//    private PrinterOptions getOptionsForFormGenerationModule(FormModule module) {
//        PrinterOptions options = new PrinterOptions();
//        APSetup apSetup = apSetupService.getApSetup();
//        if(Objects.nonNull(apSetup.getCheckPrintSetting()) && Objects.nonNull(apSetup.getCheckPrintSetting().getScalePercentForFormGeneration())
//                && (module.equals(FormModule.FORM_GENERATION))) {
//            options.setScalePercent(apSetup.getCheckPrintSetting().getScalePercentForFormGeneration());
//            options.setPageFit(false);
//        }
//
//        options.setPaperSize(DEFAULT_PAPER_SIZE_LETTER);
//        return options;
//    }


    private String exportPdfUrl() {
        String host =  getBaseUrlForPdfExport();
        return host + "/exports/pdf";
    }

    private String exportBulkPdfUrl() {
        String host = getBaseUrlForPdfExport();
        return host + TConstants.PDF_EXPORT_URL;
    }

    private String getBaseUrlForPdfExport(){

        String NEW_PDF_EXPORT_BASE_PATH_ENV_NAME = "export_host";

        String host = System.getenv(NEW_PDF_EXPORT_BASE_PATH_ENV_NAME);
        if(host == null  || host.isEmpty()){
            host = System.getenv("config_host");
        }
        return host;
    }

    private String exportPrintUrl() {
        ServerConfigFlat tekion_cloud_print = new ServerConfigServiceImpl ( ).findServerConfig ( ServerType.SERVICE.name ( ),
                "TEKION_CLOUD_PRINT", NO_TENANT_ID, NO_DEALER_ID );
        return tekion_cloud_print.getServiceHostURL ();
    }


    private String fetchMediaUrl() {
        ServerConfigFlat serverConfig = new ServerConfigServiceImpl ( ).findServerConfig ( "SERVICE", "DMS",
                UserContextProvider.getDSETenantName ( ), NO_DEALER_ID );
        String host =  serverConfig.getServiceHostURL ();
        host += "/media/r/u/presignedurls";
        return host;
    }

    private String printBulkPdfUrl(){
        String BULK_PDF_PRINT_URL = "/pms/u/v3/print";
        String host = System.getenv("config_host");

        return host + BULK_PDF_PRINT_URL;
    }

	public static HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.addAll(TRequestUtils.userCallHttpHeaders());
		return headers;
	}
}
