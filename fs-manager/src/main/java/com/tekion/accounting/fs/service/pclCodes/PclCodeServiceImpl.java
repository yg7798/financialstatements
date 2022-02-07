package com.tekion.accounting.fs.service.pclCodes;

import com.google.common.collect.Maps;
import com.poiji.bind.Poiji;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.dto.pclCodes.*;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.repos.OemTemplateRepo;
import com.tekion.accounting.fs.service.common.FileCommons;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        accountingOemFsCellGroup.updateGroupCodes(pclDetails);
        oemFsCellGroupRepo.save(accountingOemFsCellGroup);
    }

    @Override
    public void updatePclCodesInBulk(MediaRequestDto requestDto) {
        File file = null;
        try{
            log.info("PCL bulk update requested for media id {}", requestDto.getMediaId());
            file = fileCommons.downloadFileUsingMediaId(requestDto.getMediaId());
            validatePclCodeUpdateFile(file);
            updatePclDetails(file);

        }catch (Exception e){
            log.error("Pcl update failed with error: {} ",e);
            throw new TBaseRuntimeException(FSError.uploadValidPclCodesFile);
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
                    || checkIfFilterTrue(pclFilters.getDealerTrackPcl(), cellGroup.getDealerTrackPcl())
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

    private boolean checkIfFilterTrue(Set<String> filterCodeList, String code) {
        if(TCollectionUtils.isEmpty(filterCodeList))
            return false;
        return filterCodeList.contains(code);
    }

    private String defaultCountryCode(String country) {
        return TStringUtils.isBlank(country) ? TConstants.COUNTRY_US : country;
    }

    private void validatePclCodeUpdateFile(File file) throws IOException {
        Workbook workbook = null;
        try{
            workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if(!rowIterator.hasNext()){
                throw new TBaseRuntimeException(FSError.uploadValidPclCodesFile);
            }

            rowIterator.next(); // skip header row

            if(!rowIterator.hasNext() ){
                throw new TBaseRuntimeException(FSError.uploadValidPclCodesFile);
            }

            Row row = rowIterator.next();
            if(row == null || row.getCell(0) == null || row.getCell(0).getCellType().equals(CellType.BLANK)){
                throw new TBaseRuntimeException(FSError.entryNumberCannotBeEmpty);
            }
            log.info("Validate pcl code update successfully");
        }catch (IOException ioException){
            log.error("Validate pcl code update failed with error: {} ",ioException);
            throw new TBaseRuntimeException(FSError.ioError);
        }finally {
            workbook.close();
        }
        log.info("PCL bulk update file validated");
    }

    private void updatePclDetails(File file) {
        Map<String, PclDetailsInExcel> pclDetailsToBeUpdatedList = Maps.newHashMap();
        Map<String, Set<String>> keyVsOemDetails = Maps.newHashMap();
        try {
            List<PclDetailsInExcel> pclDetailsInExcelList = TCollectionUtils.nullSafeList(Poiji.fromExcel(file, PclDetailsInExcel.class));
            getPclDetailsToUpdateFromFile(pclDetailsInExcelList, keyVsOemDetails, pclDetailsToBeUpdatedList);

            if(TCollectionUtils.isEmpty(pclDetailsToBeUpdatedList)){
                log.error("ERROR: Invalid pcl details to update");
                throw new TBaseRuntimeException(FSError.uploadValidPclCodesFile);
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
            throw new TBaseRuntimeException(FSError.uploadValidPclCodesFile);
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
                accountingOemFsCellGroup.updateGroupCodes(pclDetailsToBeUpdatedList.get(entry.getKey()));
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
