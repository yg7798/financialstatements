package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.accountingInfo.FsOemPayloadInfo;
import com.tekion.accounting.fs.beans.common.*;
import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.enums.Month;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.cellcode.FsCodeDetail;
import com.tekion.accounting.fs.dto.integration.FSIntegrationRequest;
import com.tekion.accounting.fs.dto.integration.FSSubmitResponse;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.*;
import com.tekion.accounting.fs.integration.*;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemConfigRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.common.slackAlert.FsSlackMessageDto;
import com.tekion.accounting.fs.service.common.slackAlert.SlackService;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.accounting.fs.service.external.nct.FillDetailContext;
import com.tekion.accounting.fs.service.fsMetaData.OemFSMetadataMappingService;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.accounting.fs.service.oemConfig.OemConfigService;
import com.tekion.accounting.fs.service.utils.FSDealerMasterUtils;
import com.tekion.admin.beans.BrandMappingResponse;
import com.tekion.admin.beans.FindBrandRequest;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.clients.preference.client.TekionResponse;
import com.tekion.core.excelGeneration.models.model.template.SingleCellData;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import com.tekion.dealersettings.dealermaster.beans.DseDealerAddress;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.propertyclient.DPClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.common.TConstants.FAILED;
import static com.tekion.accounting.fs.enums.OemCellValueType.PERC;
import static org.apache.commons.lang.StringUtils.EMPTY;

@AllArgsConstructor
@Slf4j
public abstract class AbstractFinancialStatementService implements FinancialStatementService{
    @Autowired
    protected FsComputeService oemMappingService;
    protected DealerConfig dealerConfig;
    protected IntegrationClient integrationClient;
    protected FsXMLServiceImpl fsXMLService;
    @Autowired
    protected DPClient dpClient;
    @Autowired
    protected AccountingInfoService infoService;
    @Autowired
    protected PreferenceClient preferenceClient;
    @Autowired
    protected OemConfigRepo oemConfigRepo;
    @Autowired
    protected AccountingService accountingService;
    @Autowired
    protected OemFSMetadataMappingService fsMetadataMappingService;
    @Autowired
    protected IntegrationService integrationService;
    @Autowired
    protected FSEntryRepo fsEntryRepo;
    @Autowired
    protected SlackService slackService;
    @Autowired
    protected GlobalService globalService;
    @Autowired
    protected AccountingInfoService accountingInfoService;
    @Autowired
    OemConfigService oemConfigService;

    public static final String MTD = "MTD";
    public static final String YTD = "YTD";

    private static final String DATE_FORMAT_PATTERN =  "MM/dd/yy";

    public AbstractFinancialStatementService(
            DealerConfig dealerConfig, IntegrationClient integrationClient, FsXMLServiceImpl fsXMLService) {
        this.dealerConfig = dealerConfig;
        this.integrationClient = integrationClient;
        this.fsXMLService = fsXMLService;
    }

    @Override
    public FSSubmitResponse submit(FinancialStatementRequestDto requestDto) {
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());
        log.info("submit FS {} {} {}", fsEntry.getOemId(), requestDto.getTillEpoch(), UserContextProvider.getCurrentDealerId());
        ProcessFinancialStatement processFinancialStatement = getFinancialStatementResponse(requestDto);
        FSIntegrationRequest fsIntegrationRequest = new FSIntegrationRequest();
        fsIntegrationRequest.setFinancialStatements(processFinancialStatement.getDataArea().getFinancialStatements());
        logRequestPayload(fsIntegrationRequest);
        OEM oem = OEM.valueOf(fsEntry.getOemId());
        List<String> brands = getBrandFromMakes(Arrays.asList(oem.getMake()));
        String brand = (brands.size() != 0 && !"Others".equals(brands.get(0))) ? brands.get(0) : oem.getBrand();
        FSSubmitResponse response = null;
        try {
            if(useDownloadApiFromIntegration(requestDto)){
                log.info("calling DownloadFinancialStatement API from integration");
                response =  integrationClient.downloadFS(fsIntegrationRequest, OEMInfo.builder().oem(oem.getOem()).brand(brand).build(), fsEntry.getSiteId());
            }else{
                response =  integrationClient.submitFS(fsIntegrationRequest, OEMInfo.builder().oem(oem.getOem()).brand(brand).build(), fsEntry.getSiteId());
            }

        }catch (Exception submissionException){
            log.error("error for submission {}: {}", oem.name(),  submissionException.getMessage());
            Map<String, String> idVsDealerNameForTenant = UserContextUtils.getDealerIdVsDealerNameForTenant(UserContextProvider.getCurrentTenantId(), globalService);
            Map<String, String> siteIdVsNameMap = UserContextUtils.getSiteIdVsNameForCurrentDealer(globalService);
            FsSlackMessageDto slackMessageDto = FsSlackMessageDto.builder()
                    .dealerId(UserContextProvider.getCurrentDealerId())
                    .dealerName(idVsDealerNameForTenant.get(UserContextProvider.getCurrentDealerId()))
                    .siteName(siteIdVsNameMap.get(UserContextUtils.getSiteIdFromUserContext()))
                    .tenantId(UserContextProvider.getCurrentTenantId())
                    .oemId(fsEntry.getOemId())
                    .status(FAILED)
                    .build();
            try {
                slackService.sendAlertForFSSubmission(slackMessageDto);
            } catch (Exception e1) {
                log.error("Exception while sending FS slack alert message {}", e1.getMessage());
            }
            throw new TBaseRuntimeException(submissionException);
        }
        return response;
    }

    @Override
    public String generateXML(FinancialStatementRequestDto requestDto) {
        throw new TBaseRuntimeException(FSError.notSupported);
    }

    @Override
    public ProcessFinancialStatement getStatement(FinancialStatementRequestDto requestDto){
        return getFinancialStatementResponse(requestDto);
    }

    /**
     * there are two cases
     * 1) Accounting BE service generate and send file to UI
     * 2) Accounting BE service submit required data to Integration service, then file wil be generated and saved in S3 by integration service
     * then UI call separate api to download that file, Porsche is an example for this case
     *
     * @param requestDto
     * @param response */
    @Override
    public void downloadFile(FinancialStatementRequestDto requestDto, HttpServletResponse response) {
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());
        OemConfig oemConfig = oemConfigRepo.findByOemId(fsEntry.getOemId(), dealerConfig.getDealerCountryCode());
        if(Objects.nonNull(oemConfig) && oemConfig.isDownloadFileFromIntegration()){
            FSSubmitResponse fsSubmitResponse = submit(requestDto);
            log.info("fsSubmitResponse {}", fsSubmitResponse);
        }else{
            throw new TBaseRuntimeException(FSError.notSupported);
        }
    }

    @Override
    public List<SingleCellData> getCellLevelFSReportData(FinancialStatementRequestDto requestDto) {
        List<SingleCellData> fsDetailsList = getCellDataListForFSReportDetails(requestDto);
        List<SingleCellData> metaDetailsList = getCellDataListForDealerInfoMapping(requestDto);
        fsDetailsList.addAll(metaDetailsList);
        return fsDetailsList;
    }

    protected List<SingleCellData> getCellDataListForFSReportDetails(FinancialStatementRequestDto requestDto) {
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());
        List<AccountingOemFsCellCode> oemFsCellCodeList = oemMappingService.getOemTMappingList(
                fsEntry.getOemId(), fsEntry.getYear(), fsEntry.getVersion(), dealerConfig.getDealerCountryCode());
        FsCellCodeDetailsResponseDto cellCodeDetailsResponse;
        switch (requestDto.getFinancialYearType()) {
            case FISCAL_YEAR: cellCodeDetailsResponse = oemMappingService.computeFsCellCodeDetailsForFS(requestDto.getFsId(),
                                    requestDto.getTillEpoch(), requestDto.isIncludeM13());
                break;
            case CALENDAR_YEAR:
            default: cellCodeDetailsResponse = oemMappingService.computeFsCellCodeDetails(fsEntry,
                                    requestDto.getTillEpoch(), requestDto.isIncludeM13(), requestDto.isAddM13BalInDecBalances());
        }
        return createCellDataList(oemFsCellCodeList, cellCodeDetailsResponse);
    }

    public List<String> getBrandFromMakes(List<String> makes) {
        if (makes.size() == 0) {
            return new ArrayList<>();
        }
        FindBrandRequest findBrandRequest = new FindBrandRequest();
        findBrandRequest.setMakes(makes);
        TekionResponse<List<BrandMappingResponse>> response = preferenceClient.findBrandForMake(findBrandRequest);
        log.info("{} Fetched brands from make {}", findBrandRequest, response);
        List<BrandMappingResponse> mappingResponses = response.getData();
        List<String> brandsList = mappingResponses.stream()
                .map(BrandMappingResponse::getActualBrand)
                .collect(Collectors.toList());
        return brandsList;

    }

    protected List<SingleCellData> getCellDataListForDealerInfoMapping(FinancialStatementRequestDto requestDto) {
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());
        List<SingleCellData> cellDataList = Lists.newArrayList();
        try {
            OemFSMetadataCellsInfo oemFSMetadataCellsInfo = fsMetadataMappingService.getOemFsMetadataCellMappings(fsEntry.getOemId(),
                    fsEntry.getYear(), fsEntry.getVersion().toString());
            List<CellAddressMapping> cellAddressMappings = Objects.nonNull(oemFSMetadataCellsInfo) ?
                    oemFSMetadataCellsInfo.getCellMapping() : Lists.newArrayList();

            Map<String, String> metadataKeyValueMap = new HashMap<>();
            getMetaDataFieldValueMap(metadataKeyValueMap, requestDto, getBacCode(fsEntry.getOemId(), fsEntry.getSiteId()));

            for(CellAddressMapping cellAddressMapping : TCollectionUtils.nullSafeList(cellAddressMappings)){
                CellAddressInfo cellAddressInfo = cellAddressMapping.getCellAddressInfo();
                String type = cellAddressMapping.getType();
                String value = metadataKeyValueMap.getOrDefault(type, EMPTY);
                SingleCellData singleCellData = new SingleCellData(
                        cellAddressInfo.getPage(),
                        cellAddressInfo.getCellAddress(),
                        value,
                        CellType.STRING);
                cellDataList.add(singleCellData);
            }
        } catch (Exception e) {
            log.error("Error while fetching cell mapping for dealerInfo, consuming and returning empty info");
            return Collections.emptyList();
        }
        return cellDataList;
    }


    private void getMetaDataFieldValueMap(Map<String, String> metadataKeyValueMap, FinancialStatementRequestDto requestDto, String bacCode){

        Calendar c = TimeUtils.buildCalendar(requestDto.getTillEpoch());
        int fiscalStartMonth_0_11 = Objects.nonNull( accountingService.getAccountingSettings())? accountingService.getAccountingSettings().getFiscalYearStartMonth(): 0;
        YearMonth fiscalYear = TimeUtils.getFYStartDetails(c.get(Calendar.YEAR), c.get(Calendar.MONTH), fiscalStartMonth_0_11);
        long fromTimeStamp = FinancialYearType.CALENDAR_YEAR.equals(requestDto.getFinancialYearType()) ?
                TimeUtils.getYearStart(requestDto.getTillEpoch()):
                TimeUtils.getMonthsStartTime(fiscalYear.getYear(), fiscalYear.getMonthValue());
        String fromTime = TimeUtils.getTimeInDateFormatFromEpoch(fromTimeStamp, DATE_FORMAT_PATTERN);
        String toTime = TimeUtils.getTimeInDateFormatFromEpoch(requestDto.getTillEpoch(), DATE_FORMAT_PATTERN);
        String fromMonth = Objects.requireNonNull(Month.monthByNumber(TimeUtils.getMonthFromEpoch(fromTimeStamp) + 1)).getDisplayName();
        String toMonth = Objects.requireNonNull(Month.monthByNumber(TimeUtils.getMonthFromEpoch(requestDto.getTillEpoch()) + 1)).getDisplayName();
        String fromDate = String.valueOf(TimeUtils.getDayFromEpoch(fromTimeStamp));
        String toDate = String.valueOf(TimeUtils.getDayFromEpoch(requestDto.getTillEpoch()));
        String fromYear = String.valueOf(TimeUtils.getYearFromEpochInDealerTimezone(fromTimeStamp));
        String toYear = String.valueOf(TimeUtils.getYearFromEpochInDealerTimezone(requestDto.getTillEpoch()));

        metadataKeyValueMap.put(OemFsMetadataFields.FROM_TIME.getDisplayName(), fromTime);
        metadataKeyValueMap.put(OemFsMetadataFields.TO_TIME.getDisplayName(), toTime);
        metadataKeyValueMap.put(OemFsMetadataFields.FROM_DATE.getDisplayName(), fromDate);
        metadataKeyValueMap.put(OemFsMetadataFields.FROM_MONTH.getDisplayName(), fromMonth);
        metadataKeyValueMap.put(OemFsMetadataFields.FROM_YEAR.getDisplayName(), fromYear);
        metadataKeyValueMap.put(OemFsMetadataFields.TO_DATE.getDisplayName(), toDate);
        metadataKeyValueMap.put(OemFsMetadataFields.TO_MONTH.getDisplayName(), toMonth);
        metadataKeyValueMap.put(OemFsMetadataFields.TO_YEAR.getDisplayName(),toYear);

        AccountingInfo accountingInfo = accountingInfoService.find(UserContextProvider.getCurrentDealerId());
        DealerMaster dealerMaster = FSDealerMasterUtils.getDealerMasterInfo(accountingInfo.getOverrideSiteInfo(), dealerConfig.getDealerMaster());
        DseDealerAddress dealerAddress = Objects.nonNull(dealerMaster.getDealerAddress()) ? dealerMaster.getDealerAddress().get(0) : null;

        metadataKeyValueMap.put(OemFsMetadataFields.DEALER_NAME.getDisplayName(), dealerMaster.getDealerName());
        metadataKeyValueMap.put(OemFsMetadataFields.DEALER_BAC_CODE.getDisplayName(), bacCode);
        metadataKeyValueMap.put(OemFsMetadataFields.ADDRESS_LINE1.getDisplayName(), Objects.nonNull(dealerAddress) ? dealerAddress.getStreetAddress1() : EMPTY);
        metadataKeyValueMap.put(OemFsMetadataFields.ADDRESS_LINE2.getDisplayName(), Objects.nonNull(dealerAddress) ? dealerAddress.getStreetAddress2() : EMPTY);
        metadataKeyValueMap.put(OemFsMetadataFields.ADDRESS_LINE3.getDisplayName(), Objects.nonNull(dealerAddress) ? dealerAddress
                .getCity() : EMPTY);
        metadataKeyValueMap.put(OemFsMetadataFields.ADDRESS_LINE4.getDisplayName(), Objects.nonNull(dealerAddress) ? dealerAddress.getState(): EMPTY);
        metadataKeyValueMap.put(OemFsMetadataFields.ADDRESS_LINE5.getDisplayName(), Objects.nonNull(dealerAddress) ? dealerAddress.getZipCode() : EMPTY);
    }

    protected List<SingleCellData> createCellDataList(List<AccountingOemFsCellCode> oemFsCellCodeList,
                                                    FsCellCodeDetailsResponseDto cellCodeDetailsResponse) {
        List<SingleCellData> excelCellItems = Lists.newArrayList();
        AtomicInteger errorCounter = new AtomicInteger();
        List<Exception> exceptionList = Lists.newArrayList();
        oemFsCellCodeList.forEach(oemFsCellCode -> {
            try {
                String code = oemFsCellCode.getCode();
                FsCodeDetail cellCodeDetail = cellCodeDetailsResponse.getCodeVsDetailsMap().get(code);
                String page = oemFsCellCode.getAdditionalInfo().get("page");
                String address = oemFsCellCode.getAdditionalInfo().get("address");
                double value = cellCodeDetail.getValue().doubleValue();
                if(PERC.toString().equalsIgnoreCase(oemFsCellCode.getValueType())) {
                    value = value / 100;
                }
                if (TStringUtils.isBlank(page) || TStringUtils.isBlank(address)) {
                    throw new TBaseRuntimeException("blank values of page and cell address in oemFsCellCode");
                }
                SingleCellData fsExcelCellItem = new SingleCellData(page, address, String.valueOf(value), CellType.NUMERIC);
                excelCellItems.add(fsExcelCellItem);
            } catch (Exception e) {
                errorCounter.getAndIncrement();
                exceptionList.add(e);
            }
        });
        return excelCellItems;
    }

    void logRequestPayload(FSIntegrationRequest fsIntegrationRequest) {
        log.info("Integration request payload!");
        log.info("{}", JsonUtil.toJson(fsIntegrationRequest));
    }

    ProcessFinancialStatement getFinancialStatementResponse(FinancialStatementRequestDto requestDto){
        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());
        FsCellCodeDetailsResponseDto fsResponse = oemMappingService.computeFsCellCodeDetails(fsEntry, requestDto.getTillEpoch(), requestDto.isIncludeM13(), requestDto.isAddM13BalInDecBalances());
        Calendar c = TimeUtils.buildCalendar(requestDto.getTillEpoch());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        OemConfig oemConfig = oemConfigService.getOemConfig(fsEntry.getOemId());
        ProcessFinancialStatement processFinancialStatement = new ProcessFinancialStatement();
        processFinancialStatement.setApplicationArea(getApplicationArea(fsEntry));
        processFinancialStatement.setDataArea(getDefaultDataArea());
        populateAccountDetails(processFinancialStatement.getDataArea(),fsResponse, year, month, oemConfig, fsEntry.getSiteId());
        return processFinancialStatement;
    }

    protected void populateAccountDetails(DataArea dataArea, FsCellCodeDetailsResponseDto fsResponse, int year, int month, OemConfig oemConfig, String siteId) {
        FinancialStatement mtdFs = new FinancialStatement();
        FinancialStatement ytdFs = new FinancialStatement();
        String dealerShipCode = getBacCode(oemConfig.getOemId(), siteId);

        Header mtdHeader = new Header();
        fillHeader(mtdHeader, year, month+1, MTD, dealerShipCode);
        mtdFs.setHeader(mtdHeader);

        Header ytdHeader = new Header();
        fillHeader(ytdHeader, year, month+1, YTD, dealerShipCode);
        ytdFs.setHeader(ytdHeader);

        Map<String, AccountingOemFsCellCode> fsCodeMap = TCollectionUtils.transformToMap(fsResponse.getAccountingOemFsCellCodes(),
                AccountingOemFsCellCode::getCode);
        Map<String, Detail> mtdOemCodeByDetail = new HashMap<>();
        Map<String, Detail> ytdOemCodeByDetail = new HashMap<>();

        Map<String, FsCodeDetail> codeVsDetailsMap = fsResponse.getCodeVsDetailsMap();
        log.info("code vs details map size {}",codeVsDetailsMap.size());

        for(String fsCellCode : codeVsDetailsMap.keySet()){
            FsCodeDetail cellValue = codeVsDetailsMap.get(fsCellCode);
            BigDecimal value = cellValue.getValue();
            AccountingOemFsCellCode actualCode = fsCodeMap.get(fsCellCode);
            if(Objects.isNull(actualCode)){
                log.error("Invalid FS cell code found {} ",fsCellCode);
                continue;
            }
            if(TStringUtils.isBlank(actualCode.getOemCode() )){
                continue;
            }

            Detail detail = null;
            if(isMTDType(actualCode)){
                detail = getDetail(mtdOemCodeByDetail, actualCode, mtdFs);
            }else{
                detail = getDetail(ytdOemCodeByDetail, actualCode, ytdFs);
            }

            FillDetailContext fdc = new FillDetailContext(value, detail, oemConfig, cellValue, actualCode, null);
            fillDetail(fdc);
        }
        log.info("mtd Header count  {} ytd Header count {}",mtdFs.getDetails().size(),ytdFs.getDetails().size());
        addExtraDetails(mtdFs, ytdFs, oemConfig.getOemId());
        mtdFs.getHeader().setCount(mtdFs.getDetails().size()+"");
        ytdFs.getHeader().setCount(ytdFs.getDetails().size()+"");
        dataArea.setFinancialStatements(Lists.newArrayList(mtdFs,ytdFs));
    }

    private Detail getDetail(Map<String, Detail> oemCodeByDetail, AccountingOemFsCellCode actualCode, FinancialStatement fs){
        Detail detail = null;
        if(Objects.isNull(oemCodeByDetail.get(actualCode.getOemCode()))){
            detail = new Detail();
            detail.setAccountId(formatTBin(actualCode));
            addDescription(detail, actualCode);
            oemCodeByDetail.put(actualCode.getOemCode(), detail);
            fs.getDetails().add(detail);
        }else{
            detail = oemCodeByDetail.get(actualCode.getOemCode());
        }

        return detail;
    }

    private String getBacCode(String oemId, String siteId){

        OEM oem = OEM.fromOem(oemId);
        List<String> brands = getBrandFromMakes(Collections.singletonList(Objects.requireNonNull(oem).getMake()));
        String brand = (brands.size() != 0 && !"Others".equals(brands.get(0))) ? brands.get(0) : oem.getBrand();

        String dealerShipCode =  integrationService.getBacCodeFromIntegration(oemId, siteId, brand);
        if(Objects.isNull(dealerShipCode)){
            dealerShipCode = dealerConfig.getDealerMaster().getOemDealerId();
            log.info("reading BAC code from dealerMaster {}", dealerShipCode);
        }
        return dealerShipCode;
    }

    protected void addDescription(Detail detail, AccountingOemFsCellCode actualCode){
        detail.setDescription(actualCode.getOemDescription());
    }

    private void fillDetail(FillDetailContext fdc){
        Map<String, String> additionalInfo = TCollectionUtils.nullSafeMap(fdc.getCellCode().getAdditionalInfo());
        fdc.getDetail().setOemCodeSign(additionalInfo.getOrDefault(AccountingOemFsCellCode.OEM_CODE_SIGN, ""));

        String valueString = "";
        if(Objects.nonNull(fdc.getCellCode().getSource()) && FsCellCodeSource.CUSTOM_SOURCE.name().equals(fdc.getCellCode().getSource().name())){
            valueString = fdc.getCellDetail().getStringValue();
        }else{
            BigDecimal newValue = getValueWithPrecision(fdc.getValue(), additionalInfo, fdc.getOemConfig());
            newValue = flipSignIfRequired(newValue, fdc.getDetail());
            valueString = newValue.toString();
        }

        fdc.setValueString(valueString);
        setValueInDetail(fdc);
    }

    protected void setValueInDetail(FillDetailContext fdc) {
        fdc.getDetail().setAccountValue(fdc.getValueString());
    }

    private boolean isMTDType(AccountingOemFsCellCode actualCode){
        //For memo worksheet which takes MTD value in YTD.
        return (Objects.nonNull(actualCode.getAdditionalInfo()) && TConstants.TRUE.equals(actualCode.getAdditionalInfo().get(TConstants.MTD_APPLICABLE)))
                || (!TConstants.TRUE.equals(actualCode.getAdditionalInfo().get(TConstants.YTD_APPLICABLE))
                && MTD.equals(actualCode.getDurationType()));
    }

    protected void addExtraDetails(FinancialStatement mtdFs, FinancialStatement ytdFs, String oemId) {
        AccountingInfo aInfo = infoService.find(UserContextProvider.getCurrentDealerId());
        if (Objects.nonNull(aInfo) && TCollectionUtils.isNotEmpty(aInfo.getOemPayloadInfos())) {
            Map<String, Collection<FsOemPayloadInfo>> payloadInfoMap = aInfo.getOemPayloadInfos().stream()
                    .collect(Collectors.groupingBy(FsOemPayloadInfo::getOem, HashMap::new, Collectors.toCollection(ArrayList::new)));

            if(Objects.isNull(payloadInfoMap.get(oemId))) return;

            for (FsOemPayloadInfo info : payloadInfoMap.get(oemId)) {
                if (MTD.equals(info.getDurationType())) {
                    if(TCollectionUtils.isNotEmpty(info.getDetails())){
                        mtdFs.getDetails().addAll(info.getDetails());
                    }

                } else if (YTD.equals(info.getDurationType())) {
                    if(TCollectionUtils.isNotEmpty(info.getDetails())){
                        ytdFs.getDetails().addAll(info.getDetails());
                    }
                }
            }
        }
    }

    protected void fillHeader(Header header, int year, int month, String accountingTerm, String dealerShipCode){
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
        String time = dateFormatter.format(new Date());
        String monthStr = month < 10 ? "0"+month: ""+month;

        header.setAccountingTerm(accountingTerm);
        header.setAccountingDate(year+ "-"+monthStr );
        header.setDocumentDateTime(time);
        header.setDealerCode(dealerShipCode);
    }

    protected String formatTBin(AccountingOemFsCellCode cellCode) {
        return cellCode.getOemCode();
    }

    DataArea getDefaultDataArea() {
        DataArea dataArea = new DataArea();
        return dataArea;
    }

    ApplicationArea getApplicationArea(FSEntry fsEntry) {
        ApplicationArea applicationArea = new ApplicationArea();
        Sender sender = new Sender();
        sender.setDealerNumber(getBacCode(fsEntry.getOemId(), fsEntry.getSiteId()));
        applicationArea.setBODId("");
        applicationArea.setCreationDateTime("");
        applicationArea.setSignature(new ApplicationArea.Signature());
        applicationArea.setSender(sender);
        Destination destination = new Destination();
        destination.setDealerNumber(getBacCode(fsEntry.getOemId(), fsEntry.getSiteId()));
        destination.setDestinationNameCode("GM");
        applicationArea.setDestination(destination);
        return applicationArea;
    }

    BigDecimal getValueWithPrecision(BigDecimal value, Map<String, String> additionalInfo, OemConfig oemConfig) {

        String prec = null;

        if(oemConfig != null && oemConfig.getDefaultPrecision() != null && !oemConfig.getDefaultPrecision().isEmpty()){
            prec = oemConfig.getDefaultPrecision();
        }

        if(additionalInfo.get(TConstants.PRECISION) != null && !additionalInfo.get(TConstants.PRECISION).isEmpty()){
            prec = additionalInfo.get(TConstants.PRECISION);
        }

        if(Objects.isNull(prec)) return value;

        try{
            int precision = Integer.parseInt(prec);
            value = value.setScale(precision,BigDecimal.ROUND_HALF_UP);
        }catch (NumberFormatException ignored){}
        return value;
    }

    BigDecimal flipSignIfRequired(BigDecimal val, Detail detail) {
        // This is only required for ford OEM
        return val;
    }

    /**
     * This is used to call separate API from integration if file type is XML for Ferrari OEM
     *  see https://tekion.atlassian.net/browse/CDMS-45328
     * */
    boolean useDownloadApiFromIntegration(FinancialStatementRequestDto requestDto){
        return false;
    }
}
