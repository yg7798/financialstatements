package com.tekion.accounting.fs.service.common.excelGeneration.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.service.common.excelGeneration.apiGateway.dto.PdfPreviewResponseDto;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.AccColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.ExcelColumnConfigGeneratorService;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.context.DynamicallyUpdateColumnConfigContext;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelFieldIdentifier;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.SupportedFormatOverrideIdentifiers;
import com.tekion.accounting.fs.service.common.cache.redis.implementation.KeywordConfigCache;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.EsReportRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.ExcelFieldFormattingConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportType;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.as.client.AccountingClient;
import com.tekion.as.models.beans.AccountingSettings;
import com.tekion.clients.preference.beans.DisplayLabel;
import com.tekion.core.excelGeneration.models.CallbackResponse;
import com.tekion.core.excelGeneration.models.model.*;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.notifcationsv2.beans.EventGroup;
import com.tekion.notifcationsv2.beans.EventType;
import com.tekion.notifcationsv2.beans.NotificationPayload;
import com.tekion.notifcationsv2.beans.NotificationType;
import com.tekion.notificationsv2.client.NotificationsV2Client;
import com.tekion.notificationsv2.client.dto.SendNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tekion.core.utils.UserContextProvider.getCurrentUserId;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelReportGeneratorHelperImpl implements ExcelReportGeneratorHelper {
    private final String SUCCESS_EXPORT_NOTIFICATION_MESSAGE = "Your export has finished processing and is ready to be downloaded.";
    private final String FAILURE_EXPORT_NOTIFICATION_MESSAGE = "Your requested export processing has failed.";
    private final String NOTIFICATION_DYNAMIC_BODY_KEY = "bodyOfMessage";
    private final String NOTIFICATION_DYNAMIC_SUBJECT_KEY = "subjectOfMessage";


    private final NotificationsV2Client notificationClient;
    private final ExcelColumnConfigGeneratorService excelColumnConfigGeneratorService;
    private final AccountingClient accountingClient;
    //private final AccountingSettingsService accountingSettingsService;
    //private final ResolveAssetUtils resolveAssetUtils;
    private final KeywordConfigCache keywordConfigCache;


    @Override
    public CallbackAcknowledge doOnErrorCallback(ErrorCallBackResponse request, String reportType) {
        log.error("ACC_EXCEL_DOWNLOAD_ERROR : error occurred while downloading report : {} , {}", reportType, JsonUtil.toJson(request));
        if(!TConstants.IS_LOCAL) {
            sendNotification(ExcelReportType.valueOf(reportType), request);
        }
        return CallbackAcknowledge.builder().acknowledged(true).build();
    }

    @Override
    public CallbackAcknowledge doOnSuccessCallback(SuccessCallbackResponse request, String reportType) {
        log.info("ReceivedSuccessCallback : This actually worked : {}, {}", JsonUtil.toJson(request), reportType);
        if(!TConstants.IS_LOCAL) {
            sendNotification(ExcelReportType.valueOf(request.getReportType()), request);
        }
        return CallbackAcknowledge.builder().acknowledged(true).build();
    }



//    @Override
    public void sendNotification(ExcelReportType reportType, SuccessCallbackResponse successCallbackResponse){
        logComputationDetailsIfNeeded(successCallbackResponse);
        String reportTypeIdentifier = reportType.name();
        String mediaId = "mediaId";
        String fileName = "fileName";
        String IS_SUCCESS_KEY = "isSuccess";

        SendNotificationRequest notificationRequest =
                buildNotificationRequest(reportTypeIdentifier
                        , Collections.singleton(getCurrentUserId())
                        , EventType.ACC_EXCEL_GENERATION);
        if (reportType.isPDFReportType()) {
            notificationRequest.setAssetType("PDF Generation");
        }

        Map<String, Object> payload = Maps.newHashMap();
        payload.put(mediaId, successCallbackResponse.getMediaUploadResponse().getMediaId());
        payload.put(fileName, successCallbackResponse.getReportOriginalFileName());
        payload.put(IS_SUCCESS_KEY, true);
        payload.put(NOTIFICATION_DYNAMIC_SUBJECT_KEY,reportType.getDisplayName() );
        payload.put(NOTIFICATION_DYNAMIC_BODY_KEY,SUCCESS_EXPORT_NOTIFICATION_MESSAGE );

        notificationRequest.setDefaultPayload(payload);

        NotificationPayload payloadForPush = new NotificationPayload();
        payloadForPush.setPushPayload(payload);
        notificationRequest.setPayloads(new HashMap<>());
        notificationRequest.getPayloads().put(NotificationType.PUSH, payloadForPush);

        notificationClient.sendRestrictedNotification(notificationRequest);
    }

    private void logComputationDetailsIfNeeded(SuccessCallbackResponse successCallbackResponse){
        if(Objects.nonNull(successCallbackResponse.getComputationDetail()) && successCallbackResponse.getComputationDetail().isComputationWithIssues()){
            log.info("Error or issues in computation: {}", JsonUtil.toJson(successCallbackResponse.getComputationDetail()));
        }
    }

//    @Override
    public void sendNotification(ExcelReportType reportType, ErrorCallBackResponse errorCallBackResponse){

        String reportTypeIdentifier = reportType.name();
        String IS_SUCCESS_KEY = "isSuccess";

        SendNotificationRequest notificationRequest =
                buildNotificationRequest(reportTypeIdentifier
                        ,Collections.singleton(getCurrentUserId())
                        , EventType.ACC_EXCEL_GENERATION);

        if (reportType.isPDFReportType()) {
            notificationRequest.setAssetType("PDF Generation");
        }
        Map<String, Object> payload = Maps.newHashMap();
        payload.put(IS_SUCCESS_KEY, false);
        payload.put(NOTIFICATION_DYNAMIC_SUBJECT_KEY, reportType.getDisplayName());
        payload.put(NOTIFICATION_DYNAMIC_BODY_KEY, FAILURE_EXPORT_NOTIFICATION_MESSAGE);

        notificationRequest.setDefaultPayload(payload);

        NotificationPayload payloadForPush = new NotificationPayload();
        payloadForPush.setPushPayload(payload);
        notificationRequest.setPayloads(new HashMap<>());
        notificationRequest.getPayloads().put(NotificationType.PUSH, payloadForPush);

        notificationClient.sendRestrictedNotification(notificationRequest);
    }


    @Override
    public String determineCorrespondingSortKeyStringToSend(ExcelReportType excelReportType, String fieldForWhichToFindMapping) {
        return excelColumnConfigGeneratorService.determineCorrespondingSortKeyToSend(excelReportType,fieldForWhichToFindMapping);
    }

    @Override
    public List<Sort> createSortToBeDoneOnLambda(String reportType, EsReportRequestDto requestDto, boolean doHandleForAmountSort) {
        List<Sort> sortToUse = Lists.newArrayList();
        if(doHandleForAmountSort) {
            Sort sort1 = ExcelGeneratorHelperUtil.removeAbsoluteAmountFromSearchRequestAndReturn(requestDto.getSearchRequest(), "amount");
            if (Objects.nonNull(sort1)) {
                sortToUse.add(sort1);
            }
        }
        for (Sort sort : TCollectionUtils.nullSafeList(requestDto.getSortList())) {
            sortToUse.add(Sort.builder()
                    .key(determineCorrespondingSortKeyStringToSend(ExcelReportType.valueOf(reportType), sort.getKey()))
                    .order(Sort.Order.valueOf(sort.getOrder().name()))
                    .build());

        }
        return sortToUse;
    }

    @Override
    public void doDynamicallyUpdateColumnFormatting(List<ColumnConfig> columnConfigList, DynamicallyUpdateColumnConfigContext dynamicallyUpdateColumnConfigContext) {

        List<ColumnConfig> columnConfigsToBeDynamicallyUpdated = columnConfigList
                .stream()
                .filter(config -> config instanceof AccColumnConfig)
                .filter(config -> Objects.nonNull(((AccColumnConfig) config).getFormatOverrideIdentifier()))
                .collect(Collectors.toList());

        if(TCollectionUtils.isEmpty(columnConfigsToBeDynamicallyUpdated)) {
            return;
        }

        Map<SupportedFormatOverrideIdentifiers, ExcelFieldFormattingConfig> mapOfCurrentColumnConfigWithOverrideApplicableForReport = getMapOfCurrentColumnConfigWithOverrideApplicableForReport(dynamicallyUpdateColumnConfigContext);

        for (ColumnConfig columnConfig : columnConfigsToBeDynamicallyUpdated) {
            SupportedFormatOverrideIdentifiers formatOverrideIdentifier = ((AccColumnConfig) columnConfig).getFormatOverrideIdentifier();
            if(mapOfCurrentColumnConfigWithOverrideApplicableForReport.containsKey(formatOverrideIdentifier)){
                ExcelFieldFormattingConfig excelFieldFormattingConfig = mapOfCurrentColumnConfigWithOverrideApplicableForReport.get(formatOverrideIdentifier);
                ExcelCellFormattingHolder cellFormattingToUse = excelFieldFormattingConfig.getOverrideType().getCellFormattingHolder();
                AccAbstractColumnConfig.applyFormattingOnColumnConfig(cellFormattingToUse,columnConfig);
            }
        }
    }

    @Override
    public void doDynamicallyUpdateLocalizedFieldName(List<ColumnConfig> columnConfigList){
        List<ColumnConfig> columnConfigsToBeDynamicallyUpdated = columnConfigList
                .stream()
                .filter(config -> config instanceof AccColumnConfig)
                .filter(config -> Objects.nonNull(((AccColumnConfig) config).getExcelFieldIdentifier()))
                .collect(Collectors.toList());

        if(TCollectionUtils.isEmpty(columnConfigsToBeDynamicallyUpdated)) {
            return;
        }
        for (ColumnConfig columnConfig : columnConfigsToBeDynamicallyUpdated) {
            ExcelFieldIdentifier excelFieldIdentifier = ((AccColumnConfig) columnConfig).getExcelFieldIdentifier();
            String columnName = getDisplayColumnName(excelFieldIdentifier);
            if(columnName != null){
                columnConfig.setColumnName(columnName);
            }
        }
    }

    private String getDisplayColumnName(ExcelFieldIdentifier excelFieldIdentifier){
        String columnName = null;
        DisplayLabel displayLabel = keywordConfigCache.getFromCache();
        if(Objects.nonNull(displayLabel)){
            columnName = KeywordConfigCache.getDisplayTextByFieldLabel(displayLabel, excelFieldIdentifier.getKey());
        }
        return columnName;
    }

    @Override
    public DynamicallyUpdateColumnConfigContext doGenerateContextForDynamicUpdate(String reportType) {
        DynamicallyUpdateColumnConfigContext dynamicallyUpdateColumnConfigContext = new DynamicallyUpdateColumnConfigContext();
        dynamicallyUpdateColumnConfigContext.setReportType(reportType);
        return dynamicallyUpdateColumnConfigContext;

    }

    // context can be used in this to generate dynamic report level, etc
    private Map<SupportedFormatOverrideIdentifiers, ExcelFieldFormattingConfig> getMapOfCurrentColumnConfigWithOverrideApplicableForReport(DynamicallyUpdateColumnConfigContext dynamicallyUpdateColumnConfigContext) {

        // TODO: 29/04/21 shift to cached map. ideally we should. can keep the way it is implemented in DpProvider
        AccountingSettings accountingSettings = accountingClient.getAccountingSettings().getData();
        if(Objects.isNull(accountingSettings)){
            return new HashMap<>();
        }
        //TODO: uncomment this
        //return TCollectionUtils.transformToMap(accountingSettings.getExcelFieldFormattingConfigList(), ExcelFieldFormattingConfig::getFormatOverrideIdentifiers);
        return new HashMap<>();
    }

    private SendNotificationRequest buildNotificationRequest(String assetId, Set<String> receiverIds, EventType eventType) {
        return SendNotificationRequest.builder()
                .requestId(new ObjectId().toString())
                .assetId(assetId)
                .assetType("Excel Generation")
                .eventType(eventType)
                .groupKey(EventGroup.ACCOUNTING +  "_" + assetId)
                .locale("en-US")
                .receiverIds(receiverIds)
                .senderId(getCurrentUserId())
                .build();
    }

    @Override
    public ColumnConfig getCopyOfColumnConfig(ColumnConfig columnConfig){
        ColumnConfig copyOfColumnConfig =new ColumnConfig();
        try{
            BeanUtils.copyProperties(copyOfColumnConfig, columnConfig);
        } catch ( IllegalAccessException | InvocationTargetException e){
            log.error("Exception while getting copy of column config:", e);
            throw new RuntimeException("Exception while getting copy of column config");
        }
        return copyOfColumnConfig;
    }

//    @Override
//    public void populateUserLookupAssetMapInContextV2(ExcelReportContextV2 context, Set<String> userIdList, ResolveAssetUtils resolveAssetUtils, LookupAsset lookUpAsset) {
//        List<LookUpDetails> lookUpDetails = Lists.newArrayList();
//
//        for (String modifiedByUserId : TCollectionUtils.nullSafeCollection(userIdList)) {
//            lookUpDetails.add(LookUpDetails.builder().idToLookUp(modifiedByUserId).lookupAsset(lookUpAsset).build());
//        }
//
//        if(TCollectionUtils.isNotEmpty(lookUpDetails)){
//            populateResolvedUserMapInContextV2(context, lookUpDetails, resolveAssetUtils);
//        }
//    }

//    private void populateResolvedUserMapInContextV2(ExcelReportContextV2 context, List<LookUpDetails> lookUpDetails, ResolveAssetUtils resolveAssetUtils) {
//        Map<LookupAsset, Map<String, JsonNode>> lookupResolvedAssetMap = resolveAssetUtils.bulkLookUpForIds(lookUpDetails);
//        for (Map.Entry<LookupAsset, Map<String, JsonNode>> lookupAssetMapEntry : lookupResolvedAssetMap.entrySet()) {
//            LookupAsset key = lookupAssetMapEntry.getKey();
//            for (Map.Entry<String, JsonNode> stringJsonNodeEntry : lookupAssetMapEntry.getValue().entrySet()) {
//                String idForLook = stringJsonNodeEntry.getKey();
//                context.getLookupAssetMap().compute(key, (key1, oldVal)->{
//                    oldVal = TCollectionUtils.nullSafeMap(oldVal);
//                    oldVal.compute(idForLook ,(key2,lookUpNode)-> stringJsonNodeEntry.getValue());
//                    return oldVal;
//                });
//            }
//        }
//    }

//    @Override
//    public Map<String, String> createUserIdVsUserNameMap(Set<String> userIdList) {
//        Map<String, String> userIdVsUserName = new HashMap<>();
//        Map<LookupAsset, Map<String, JsonNode>> lookupResolvedAssetMap = TCollectionUtils.nullSafeMap(resolveAssetUtils.getLookUpDetailsByIds(userIdList, LookupAsset.DEALER_USER));
//        if(TCollectionUtils.isEmpty(lookupResolvedAssetMap) || TCollectionUtils.isEmpty(userIdList))
//        {
//            return userIdVsUserName;
//        }
//        for(String userId : userIdList){
//            if(TStringUtils.isBlank(userId) || TConstants.SYSTEM_USER.equalsIgnoreCase(userId) || !resolveAssetUtils.validateAsset(lookupResolvedAssetMap, LookupAsset.DEALER_USER, userId)){
//                continue;
//            }
//            populateUserIdVsUserNameMap(userIdVsUserName, lookupResolvedAssetMap, userId);
//        }
//        return userIdVsUserName;
//    }

    @Override
    public Object getSyncResponseForPdfPreview(CallbackResponse response, String reportType, Object originalPayload) {
        if(response instanceof SuccessCallbackResponse){
            PdfPreviewResponseDto previewResponseDto = new PdfPreviewResponseDto();
            SuccessCallbackResponse response1 = (SuccessCallbackResponse) response;
            previewResponseDto.setMediaId(response1.getMediaUploadResponse().getMediaId());
            previewResponseDto.setOriginalRequestDto(originalPayload);
            previewResponseDto.setJobId(response1.getJobId());
            return previewResponseDto;
        }
        log.error("failed to fecth response for PDF preview from api " +
                "gateway : reportType ::: {} ::: response {}",reportType, JsonUtil.toJson(response));
        throw new TBaseRuntimeException();
    }

//    private void populateUserIdVsUserNameMap(Map<String, String> userIdVsUserName, Map<LookupAsset, Map<String, JsonNode>> lookupResolvedAssetMap, String userId) {
//        String modifiedByUser;
//        String employeeDisplayNumber = GeneralUtils.isValidNode(UserParser.getEmployeeDisplayNumber(lookupResolvedAssetMap, userId)) ?
//                UserParser.getEmployeeDisplayNumber(lookupResolvedAssetMap, userId).textValue() :  TStringUtils.EMPTY;
//        String displayName  = GeneralUtils.isValidNode(UserParser.getDisplayName(lookupResolvedAssetMap, userId))
//                ?  UserParser.getDisplayName(lookupResolvedAssetMap, userId).textValue() : TStringUtils.EMPTY;
//        if(TStringUtils.isBlank(employeeDisplayNumber) && TStringUtils.isBlank(displayName)) {
//            return;
//        } else if(TStringUtils.isBlank(employeeDisplayNumber) || TStringUtils.isBlank(displayName)){
//            modifiedByUser = TStringUtils.isBlank(employeeDisplayNumber) ? displayName : employeeDisplayNumber;
//        } else{
//            modifiedByUser = Strings.join(org.assertj.core.util.Lists.newArrayList(employeeDisplayNumber,displayName),"-");
//        }
//        userIdVsUserName.put(userId, modifiedByUser);
//    }
}
