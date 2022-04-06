package com.tekion.accounting.fs.service.pclCodes;

import com.google.common.collect.Maps;
import com.poiji.bind.Poiji;
import com.tekion.accounting.fs.auditevents.AccountingOemFsCellGroupAuditEvent;
import com.tekion.accounting.fs.auditevents.PclCodesAuditEventHelper;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.accounting.fs.dto.pclCodes.*;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.repos.OemTemplateRepo;
import com.tekion.accounting.fs.service.common.FileCommons;
import com.tekion.accounting.fs.service.externalService.media.MediaInteractorService;
import com.tekion.audit.client.manager.AuditEventManager;
import com.tekion.audit.client.manager.impl.AuditEventDTO;
import com.tekion.core.beans.TResponse;
import com.tekion.core.excelGeneration.models.model.MediaUploadResponse;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.exportable.lib.metadata.ClassMetaDataHolder;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.media.beans.response.PreSignedV2Response;
import com.tekion.media.library.MediaClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.service.utils.ExcelUtils.validateExcelFile;
import static com.tekion.core.utils.UserContextProvider.*;

@Slf4j
@Service
@AllArgsConstructor
public class PclCodeServiceImpl implements PclCodeService{

    private final String OEM_IDS = "oemIds";
    private final String COUNTRIES = "countries";
    private final String GROUP_CODES = "groupCodes";
    private final String YEARS = "years";
    private OemTemplateRepo oemTemplateRepo;
    private OemFsCellGroupRepo oemFsCellGroupRepo;
    private FileCommons fileCommons;
    private final MediaInteractorService mediaInteractorService;
    private final MediaClient mediaClient;
    private final AuditEventManager auditEventManager;

    @Override
    public List<OemDetailsResponseDto> getOemDetails() {

        List<OemTemplate> oemTemplateList = TCollectionUtils.nullSafeList(oemTemplateRepo.findAllOemDetails());
        List<OemDetailsResponseDto> oemDetailsDtoList = new ArrayList<>();
        oemTemplateList.stream().forEach(oemTemplate -> {
            OemDetailsResponseDto oemDetailsDto = new OemDetailsResponseDto();
            oemDetailsDto.setOemId(oemTemplate.getOemId());
            oemDetailsDto.setYear(oemTemplate.getYear());
            oemDetailsDto.setCountry(oemTemplate.getCountry());
            if(!oemDetailsDtoList.contains(oemDetailsDto))
                oemDetailsDtoList.add(oemDetailsDto);
            });
        return oemDetailsDtoList;
    }

    @Override
    public List<AccountingOemFsCellGroup> getPclCodeDetails(String oemId, Integer year, String country) {
        country = defaultCountryCode(country);
        List<AccountingOemFsCellGroup> accountingOemFsCellGroupList =
                TCollectionUtils.nullSafeList(oemFsCellGroupRepo.findByOemId(oemId, year, country));
        for (AccountingOemFsCellGroup cellGroup:
             accountingOemFsCellGroupList) {
            cellGroup.setDealerTrackPcl(cellGroup.getOemAccountNumber());
        }
        return accountingOemFsCellGroupList;
    }

    @Override
    public void updatePclCodeDetails(AccountingOemFsCellGroup pclDetails) {
        AccountingOemFsCellGroup accountingOemFsCellGroup = oemFsCellGroupRepo
                .findByGroupCode(pclDetails.getOemId(), pclDetails.getYear(),
                        pclDetails.getGroupCode(), pclDetails.getCountry());
        if(Objects.isNull(accountingOemFsCellGroup)) {
            throw new TBaseRuntimeException("This combination of pclCode update does not exist {}, {}, {}, {}", pclDetails.getOemId(), pclDetails.getYear().toString(), pclDetails.getCountry(), pclDetails.getGroupCode());
        }
        accountingOemFsCellGroup.updatePclCodes(pclDetails);
        AccountingOemFsCellGroup cellGroup = oemFsCellGroupRepo.save(accountingOemFsCellGroup);

        AccountingOemFsCellGroupAuditEvent fsCellGroupAuditEvent = cellGroup.populateOemFsCellGroupAuditEvent();
        AuditEventDTO fsCellGroupEventDto = PclCodesAuditEventHelper.getAuditEvent(fsCellGroupAuditEvent);
        auditEventManager.publishEvents(fsCellGroupEventDto);
    }

    @Override
    public void updatePclCodesInBulk(MediaRequestDto requestDto) {
        File file = null;
        try{
            log.info("PCL bulk update requested for media id {}", requestDto.getMediaId());
            file = fileCommons.downloadFileUsingMediaId(requestDto.getMediaId());
            validateExcelFile(file);
            updatePclDetails(file);

        }catch (Exception e){
            log.error("Pcl update failed with error: ", e);
            throw new TBaseRuntimeException(FSError.uploadValidExcelFile);
        }finally {
            if(Objects.nonNull(file)) {
                file.delete();
            }
        }
    }

    @Override
    public List<AccountingOemFsCellGroup> getOemDetailsWithFilter(PclFilterRequestDto requestDto) {
        PclFilters pclFilters = requestDto.getFilters();
        List<AccountingOemFsCellGroup> cellGroupList = TCollectionUtils.nullSafeList(getPclCodeDetails(requestDto.getOemId(), requestDto.getYear(), requestDto.getCountry()));
        List<AccountingOemFsCellGroup> filteredList = new ArrayList<>();
        cellGroupList.stream().forEach(cellGroup -> {

            if(checkIfFilterTrue(pclFilters.getGroupCode(), cellGroup.getGroupCode())
                    || checkIfFilterTrue(pclFilters.getAutomatePcl(), cellGroup.getAutomatePcl())
                    || checkIfFilterTrue(pclFilters.getAutosoftPcl(), cellGroup.getAutosoftPcl())
                    || checkIfFilterTrue(pclFilters.getCdkPcl(), cellGroup.getCdkPcl())
                    || checkIfFilterTrue(pclFilters.getDbPcl(), cellGroup.getDbPcl())
                    || checkIfFilterTrue(pclFilters.getDealerTrackPcl(), cellGroup.getOemAccountNumber())
                    || checkIfFilterTrue(pclFilters.getDominionPcl(), cellGroup.getDominionPcl())
                    || checkIfFilterTrue(pclFilters.getGroupDisplayName(), cellGroup.getGroupDisplayName())
                    || checkIfFilterTrue(pclFilters.getPbsPcl(), cellGroup.getPbsPcl())
                    || checkIfFilterTrue(pclFilters.getQuorumPcl(), cellGroup.getQuorumPcl())
                    || checkIfFilterTrue(pclFilters.getRrPcl(), cellGroup.getRrPcl())) {
                filteredList.add(cellGroup);
            }
        });
        return filteredList;
    }

    @Override
    public Map<String, String> downloadOemDetailsAndProvidePresignedUrl(PclDownloadRequestDto requestDto) {
        List<AccountingOemFsCellGroup> accountingOemFsCellGroupList = oemFsCellGroupRepo.findByOemId(requestDto.getOemId(), requestDto.getYear(), requestDto.getCountry());
        List<PclUpdateExcelDto> pclUpdateExcelDtoList = new ArrayList<>();

        accountingOemFsCellGroupList.stream().forEach(oemFsCellGroup -> {
            pclUpdateExcelDtoList.add(PclUpdateExcelDto.builder()
                    .groupDisplayName(oemFsCellGroup.getGroupDisplayName())
                    .pclCode(requestDto.getDmsType().getPclCode(oemFsCellGroup))
                .build());
        });

        changeColumnName(requestDto.getDmsType());

        if(pclUpdateExcelDtoList.size()<=0)
            throw new TBaseRuntimeException("Error while uploading media!");
        MediaUploadResponse mediaUploadResponse = mediaInteractorService.getMediaUploadResponse(pclUpdateExcelDtoList, PclUpdateExcelDto.class);
        if(Objects.isNull(mediaUploadResponse) || TStringUtils.isBlank(mediaUploadResponse.getMediaId())){
            throw new TBaseRuntimeException("Error while uploading media!");
        }
        Set<String> mediaIds = new HashSet<>();
        mediaIds.add(mediaUploadResponse.getMediaId());
        TResponse<List<PreSignedV2Response>> signedUrl = mediaClient.bulkPreSignedUrlsV2(mediaIds);
        if(Objects.isNull(signedUrl) || Objects.isNull(signedUrl.getData()))
            throw new TBaseRuntimeException("Error while getting preSigned URL!");
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("preSignedUrl", signedUrl.getData().get(0).getNormal().getUrl());
        responseMap.put("oemId", requestDto.getOemId());
        responseMap.put("year",  requestDto.getYear().toString());
        responseMap.put("country", requestDto.getCountry());
        responseMap.put("dmsType", requestDto.getDmsType().name());
        return responseMap;
    }

    private void changeColumnName(PclCodeEnum dmsType) {
        try {
            Field field = PclUpdateExcelDto.class.getDeclaredField("pclCode");
            ClassMetaDataHolder.getOrGenerateBeanMetaDataFromMap(PclUpdateExcelDto.class).getExcelFieldMetaDataMap().get(field).setExcelFieldName(dmsType.name());
        } catch (Exception e){
            log.error("Exception occured while changing column name of excel: ", e);
        }
    }

    @Override
    public String updatePclCodes(MediaRequestDto requestDto) {
        File file = null;
        try{
            log.info("PCL bulk update requested for media id {} and dmsType {}", requestDto.getMediaId(), requestDto.getDmsType());
            file = fileCommons.downloadFileUsingPresignedUrl(requestDto.getPreSignedUrl());
            validateExcelFile(file);
            updatePclDetailByGroupCodeAndOemDetails(file, requestDto);

        }catch (Exception e){
            log.error("Pcl update failed with error: {} ",e);
            throw new TBaseRuntimeException(FSError.uploadValidExcelFile);
        }finally {
            if(Objects.nonNull(file)) {
                file.delete();
            }
        }
        return "success";
    }

    private void updatePclDetailByGroupCodeAndOemDetails(File file, MediaRequestDto requestDto) {
        List<AccountingOemFsCellGroup> accountingOemFsCellGroupList = getPclCodeDetails(requestDto.getOemId(), requestDto.getYear(), requestDto.getCountry());
        List<AccountingOemFsCellGroup> updateCellGroupsInDbList = new ArrayList<>();
        try {
            Map<String, AccountingOemFsCellGroup> groupCodeVsFsCellGroupDetailDBMap = TCollectionUtils.transformToMap(accountingOemFsCellGroupList, AccountingOemFsCellGroup::getGroupDisplayName);
            List<PclUpdateExcelDto> pclUpdateDetails = TCollectionUtils.nullSafeList(Poiji.fromExcel(file, PclUpdateExcelDto.class));
            Map<String, String> groupCodeVsPclDetailMap = new HashMap<>();
            pclUpdateDetails.stream().forEach(pclUpdateDetail -> {
                groupCodeVsPclDetailMap.put(pclUpdateDetail.getGroupDisplayName(), pclUpdateDetail.getPclCode());
            });
            for ( Map.Entry<String, String> groupCodeVsPclDetail: groupCodeVsPclDetailMap.entrySet()) {
                if(groupCodeVsFsCellGroupDetailDBMap.containsKey(groupCodeVsPclDetail.getKey())){
                    requestDto.getDmsType().setPclCode(groupCodeVsFsCellGroupDetailDBMap.get(groupCodeVsPclDetail.getKey()), groupCodeVsPclDetail.getValue());
                    updateCellGroupsInDbList.add(groupCodeVsFsCellGroupDetailDBMap.get(groupCodeVsPclDetail.getKey()));
                }
            }
            oemFsCellGroupRepo.upsertBulk(updateCellGroupsInDbList);
            log.info("Pcl codes updated on db: {}", updateCellGroupsInDbList.size());

            updateCellGroupsInDbList.stream().forEach(fsCellGroup -> {
                AccountingOemFsCellGroupAuditEvent fsCellGroupAuditEvent = fsCellGroup.populateOemFsCellGroupAuditEvent();
                AuditEventDTO fsCellGroupEventDto = PclCodesAuditEventHelper.getAuditEvent(fsCellGroupAuditEvent);
                auditEventManager.publishEvents(fsCellGroupEventDto);
            });
        } catch (Exception e){
            log.error("Error while updating pcl details {}", e);
        }
    }

    private boolean checkIfFilterTrue(Set<String> filterCodeList, String code) {
        if(TCollectionUtils.isEmpty(filterCodeList))
            return false;
        return filterCodeList.contains(code);
    }

    private String defaultCountryCode(String country) {
        return TStringUtils.isBlank(country) ? TConstants.COUNTRY_US : country;
    }

    private void updatePclDetails(File file) {
        Map<String, PclDetailsInExcel> pclDetailsToBeUpdatedList = Maps.newHashMap();
        Map<String, Set<String>> keyVsOemDetails = Maps.newHashMap();
        try {
            List<PclDetailsInExcel> pclDetailsInExcelList = TCollectionUtils.nullSafeList(Poiji.fromExcel(file, PclDetailsInExcel.class));
            getPclDetailsToUpdateFromFile(pclDetailsInExcelList, keyVsOemDetails, pclDetailsToBeUpdatedList);

            if(TCollectionUtils.isEmpty(pclDetailsToBeUpdatedList)){
                log.error("ERROR: Invalid pcl details to update");
                throw new TBaseRuntimeException(FSError.uploadValidExcelFile);
            }

            Set<Integer> years = convertIntegersToString(keyVsOemDetails.get(YEARS));
            List<AccountingOemFsCellGroup> oemFsCellGroupList = TCollectionUtils.nullSafeList(oemFsCellGroupRepo
                    .findByGroupCode(keyVsOemDetails.get(OEM_IDS), years, keyVsOemDetails.get(COUNTRIES), keyVsOemDetails.get(GROUP_CODES)));

            Map<String, AccountingOemFsCellGroup> keyListToFetchFromDb = oemFsCellGroupList.stream().collect(Collectors.toMap(
                    k -> getKeyAfterConcatenate(k.getOemId(), k.getYear().toString(), k.getCountry(), k.getGroupCode()), value -> value , (oldVal,newVal) -> oldVal));

            List<AccountingOemFsCellGroup> accountingOemFsCellGroupList = getAccountingOemFsCellGroupList(pclDetailsToBeUpdatedList, keyListToFetchFromDb);
            oemFsCellGroupRepo.upsertBulk(accountingOemFsCellGroupList);
        }catch (IOException io) {
            log.error("ERROR: update pcl details {}", io);
            throw new TBaseRuntimeException(FSError.ioError);
        } catch (Exception e) {
            log.error("ERROR: update pcl details {}", e);
            throw new TBaseRuntimeException(FSError.uploadValidExcelFile);
        }
    }

    private Set<Integer> convertIntegersToString(Set<String> inputInIntegers) {
        Set<Integer> years;
        try {
           years = inputInIntegers.stream().map(k -> Integer.parseInt(k)).collect(Collectors.toSet());
           return years;
        } catch (NumberFormatException ne) {
            throw new NumberFormatException("ERROR Update PCLs: Could not transform to number from string");
        }
    }

    private List<AccountingOemFsCellGroup> getAccountingOemFsCellGroupList(Map<String, PclDetailsInExcel> pclDetailsToBeUpdatedList, Map<String, AccountingOemFsCellGroup> keyListToFetchFromDb) {
        List<AccountingOemFsCellGroup> accountingOemFsCellGroupList = new ArrayList<>();
        for (Map.Entry<String, PclDetailsInExcel> entry: pclDetailsToBeUpdatedList.entrySet()) {
            if(keyListToFetchFromDb.containsKey(entry.getKey())) {
                AccountingOemFsCellGroup accountingOemFsCellGroup = keyListToFetchFromDb.get(entry.getKey());
                accountingOemFsCellGroup.updatePclCodes(pclDetailsToBeUpdatedList.get(entry.getKey()));
                accountingOemFsCellGroupList.add(accountingOemFsCellGroup);
            }
        }
        return accountingOemFsCellGroupList;
    }

    private void getPclDetailsToUpdateFromFile(List<PclDetailsInExcel> pclDetailsInExcelList, Map<String, Set<String>> keyVsOemDetails, Map<String, PclDetailsInExcel> pclDetailsList) throws IOException {

       if(TCollectionUtils.isEmpty(pclDetailsInExcelList))
           return;

       for(int index = 0; index < pclDetailsInExcelList.size(); index++) {
           PclDetailsInExcel pclDetailsInExcel = pclDetailsInExcelList.get(index);
           if(!(TStringUtils.isBlank(pclDetailsInExcel.getOEM_ID()) || TStringUtils.isBlank(pclDetailsInExcel.getYEAR())
                    || TStringUtils.isBlank(pclDetailsInExcel.getCOUNTRY()) || TStringUtils.isBlank(pclDetailsInExcel.getGROUP_CODE()))) {
               keyVsOemDetails.computeIfAbsent(OEM_IDS, k -> new HashSet<>()).add(pclDetailsInExcel.getOEM_ID());
               keyVsOemDetails.computeIfAbsent(YEARS, k -> new HashSet<>()).add(pclDetailsInExcel.getYEAR());
               keyVsOemDetails.computeIfAbsent(COUNTRIES, k -> new HashSet<>()).add(pclDetailsInExcel.getCOUNTRY());
               keyVsOemDetails.computeIfAbsent(GROUP_CODES, k -> new HashSet<>()).add(pclDetailsInExcel.getGROUP_CODE());
               pclDetailsList.put(getKeyAfterConcatenate(pclDetailsInExcel.getOEM_ID(), pclDetailsInExcel.getYEAR(),
                       pclDetailsInExcel.getCOUNTRY(), pclDetailsInExcel.getGROUP_CODE()), pclDetailsInExcel);
           }
       }
    }

    private String getKeyAfterConcatenate(String oemId, String year, String country, String groupCode) {
        return oemId + "_" + year + "_" + country + "_" + groupCode;
    }
}
