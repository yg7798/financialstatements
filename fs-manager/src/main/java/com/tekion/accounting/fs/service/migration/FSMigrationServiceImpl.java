package com.tekion.accounting.fs.service.migration;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.repos.*;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetTemplateRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetTemplateRepo;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.accounting.fs.service.worksheet.HCWorksheetService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetService;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FSMigrationServiceImpl implements FSMigrationService {

    private final OEMFinancialMappingRepository oemFinancialMappingRepo;
    private final OemFSMappingRepo oemFSMappingRepo;
    private final MemoWorksheetRepo memoWorksheetRepo;
    private final HCWorksheetRepo hcWorksheetRepo;
    private final OEMFsCellCodeSnapshotRepo oemFsCellCodeSnapshotRepo;
    private final OemFsMappingSnapshotRepo oemFsMappingSnapshotRepo;
    private final FSEntryRepo fsEntryRepo;
    private final FsComputeService oemMappingService;
    private final MemoWorksheetService memoWorksheetService;
    private final HCWorksheetService hcWorksheetService;
    private final OemTemplateRepo oemTemplateRepo;
    private final MemoWorksheetTemplateRepo memoWorksheetTemplateRepo;
    private final HCWorksheetTemplateRepo hcWorksheetTemplateRepo;
    private final FSCellCodeRepo fsCellCodeRepo;
    private final OemFsCellGroupRepo oemFsCellGroupRepo;
    private final OemConfigRepo oemConfigRepo;
    private final OemFsMetadataCellMappingRepo oemFsMetadataCellMappingRepo;

    @Override
    public void setSiteIdInOemFinancialMapping(){
        log.info("started migration setSiteIdInOemFinancialMapping for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        oemFinancialMappingRepo.updateDefaultSiteId(UserContextProvider.getCurrentDealerId());
        log.info("finished migration setSiteIdInOemFinancialMapping for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void setSiteIdInOemFsMapping(){
        log.info("started migration setSiteIdInOemFsMapping for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        oemFSMappingRepo.updateDefaultSiteId(UserContextProvider.getCurrentDealerId());
        log.info("finished migration setSiteIdInOemFsMapping for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void setSiteIdInOemFsMappingInfo(){
        log.info("started migration setSiteIdInOemFsMappingInfo for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        fsEntryRepo.updateDefaultSiteId(UserContextProvider.getCurrentDealerId());
        log.info("finished migration setSiteIdInOemFsMappingInfo for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void setSiteIdInOEMFsCellCodeSnapshot(){
        log.info("started migration setSiteIdInOEMFsCellCodeSnapshot for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        oemFsCellCodeSnapshotRepo.updateDefaultSiteId(UserContextProvider.getCurrentDealerId());
        log.info("finished migration setSiteIdInOEMFsCellCodeSnapshot for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void setSiteIdInOemFsMappingSnapshot(){
        log.info("started migration setSiteIdInOemFsMappingSnapshot for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        oemFsMappingSnapshotRepo.updateDefaultSiteId(UserContextProvider.getCurrentDealerId());
        log.info("finished migration setSiteIdInOemFsMappingSnapshot for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void setSiteIdInMemoWorksheet(){
        log.info("started migration setSiteIdInMemoWorksheet for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        memoWorksheetRepo.updateDefaultSiteId(UserContextProvider.getCurrentDealerId());
        log.info("finished migration setSiteIdInMemoWorksheet for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void setSiteIdInHeadCountWorksheet(){
        log.info("started migration setSiteIdInHeadCountWorksheet for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        hcWorksheetRepo.updateDefaultSiteId(UserContextProvider.getCurrentDealerId());
        log.info("finished migration setSiteIdInHeadCountWorksheet for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void setFsTypeInFsEntries(){
        log.info("started migration setFsTypeInFsEntries for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        List<FSEntry> fsEntryList = fsEntryRepo.getFSEntries(UserContextProvider.getCurrentDealerId());
        for(FSEntry fsEntry : TCollectionUtils.nullSafeList(fsEntryList)){
            if(fsEntry.getOemId().contains("Internal")){
                fsEntry.setFsType(FSType.INTERNAL.name());
                fsEntry.setOemId(fsEntry.getOemId().substring(0, fsEntry.getOemId().indexOf("Internal")));
            }
            else{
                fsEntry.setFsType(FSType.OEM.name());
            }
            fsEntry.setModifiedByUserId(UserContextProvider.getCurrentUserId());
            fsEntry.setModifiedTime(System.currentTimeMillis());
        }
        fsEntryRepo.bulkUpsert(fsEntryList);
        log.info("finished migration setFsTypeInFsEntries for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void migrateOemFsMappingFromOemToFSLevel() {
        log.info("started migration OemFsMappingFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        oemMappingService.migrateOemFsMappingFromOemToFSLevel(UserContextProvider.getCurrentDealerId());
        log.info("finished migration OemFsMappingFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void migrateMemoWorksheetFromOemToFSLevel() {
        log.info("started migration MemoWorksheetFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        memoWorksheetService.migrateMemoWorksheetFromOemToFSLevel(UserContextProvider.getCurrentDealerId());
        log.info("finished migration MemoWorksheetFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void migrateHeadCountWorksheetFromOemToFSLevel() {
        log.info("started migration HeadCountWorksheetFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        hcWorksheetService.migrateHeadCountWorksheetFromOemToFSLevel(UserContextProvider.getCurrentDealerId());
        log.info("finished migration HeadCountWorksheetFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void migrateOemFsCellCodeSnapshotsFromOemToFSLevel() {
        log.info("started migration OemFsCellCodeSnapshotsFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        oemMappingService.migrateOemFsCellCodeSnapshotsFromOemToFSLevel(UserContextProvider.getCurrentDealerId());
        log.info("finished migration OemFsCellCodeSnapshotsFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void migrateOemFsMappingSnapshotsFromOemToFSLevel() {
        log.info("started migration OemFsMappingSnapshotsFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        oemMappingService.migrateOemFsMappingSnapshotsFromOemToFSLevel(UserContextProvider.getCurrentDealerId());
        log.info("finished migration OemFsMappingSnapshotsFromOemToFSLevel for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void addFsTypeInOemFsCellCodeSnapshots() {
        log.info("started migration addFsTypeInOemFsCellCodeSnapshots for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
        oemMappingService.addFsTypeInOemFsCellCodeSnapshots(UserContextProvider.getCurrentDealerId());
        log.info("finished migration addFsTypeInOemFsCellCodeSnapshots for dealer ::: tenant ::: {} , {} ", UserContextProvider.getCurrentDealerId() ,UserContextProvider.getCurrentTenantId());
    }

    @Override
    public void addCountryInOemTemplate() {
        log.info("started migration for addCountryInOemTemplate");
        oemTemplateRepo.addCountryInOemTemplate();
        log.info("finished migration for addCountryInOemTemplate");
    }

    @Override
    public void addCountryInMemoWorksheetTemplate() {
        log.info("started migration for addCountryInMemoWorksheetTemplate");
        memoWorksheetTemplateRepo.addCountryInMemoWorksheetTemplate();
        log.info("finished migration for addCountryInMemoWorksheetTemplate");
    }

    @Override
    public void addCountryInHeadCountWorksheetTemplate() {
        log.info("started migration for addCountryInHeadCountWorksheetTemplate");
        hcWorksheetTemplateRepo.addCountryInHeadCountWorksheetTemplate();
        log.info("started migration for addCountryInHeadCountWorksheetTemplate");
    }

    @Override
    public void addCountryInOemFsCellCodes() {
        log.info("started migration for addCountryInOemFsCellCodes");
        fsCellCodeRepo.addCountryInOemFsCellCodes();
        log.info("started migration for addCountryInOemFsCellCodes");
    }

    @Override
    public void addCountryInOemFsCellGroupCodes() {
        log.info("started migration for addCountryInOemFsCellGroupCodes");
        oemFsCellGroupRepo.addCountryInOemFsCellGroupCodes();
        log.info("started migration for addCountryInOemFsCellGroupCodes");
    }

    @Override
    public void addCountryInOemConfigs() {
        log.info("started migration for addCountryInOemConfigs");
        oemConfigRepo.addCountryInOemConfigs();
        log.info("started migration for addCountryInOemConfigs");
    }

    @Override
    public void addCountryInFsExcelMetaDataMappingInfo() {
        log.info("started migration for addCountryInOemConfigs");
        oemFsMetadataCellMappingRepo.addCountryInOemFsMetaDataMappings();
        log.info("started migration for addCountryInOemConfigs");
    }

}
