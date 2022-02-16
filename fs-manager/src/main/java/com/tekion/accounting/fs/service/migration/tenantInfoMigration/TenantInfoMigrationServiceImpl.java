package com.tekion.accounting.fs.service.migration.tenantInfoMigration;

import com.tekion.accounting.fs.beans.mappings.OemFsMappingSnapshot;
import com.tekion.accounting.fs.repos.*;
import com.tekion.accounting.fs.repos.accountingInfo.AccountingInfoRepo;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetRepo;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantInfoMigrationServiceImpl implements TenantInfoMigrationService {

    private final FSEntryRepo fsEntryRepo;
    private final HCWorksheetRepo hcWorksheetRepo;
    private final MemoWorksheetRepo memoWorksheetRepo;
    private final AccountingInfoRepo accountingInfoRepo;
    private final OEMFinancialMappingMediaRepository oemFinancialMappingMediaRepository;
    private final OEMFinancialMappingRepository oemFinancialMappingRepository;
    private final OEMFsCellCodeSnapshotRepo oemFsCellCodeSnapshotRepo;
    private final OemFSMappingRepo oemFSMappingRepo;
    private final OemFsMappingSnapshotRepo oemFsMappingSnapshotRepo;

    @Override
    public Integer addTenantDealerInfoInMongoBeans(MongoCollectionEnum mongoCollectionEnum) {
        log.info("Started migration for dealerTenantInfo in {} for dealerId {}, tenantId {}", mongoCollectionEnum.name(), UserContextProvider.getCurrentDealerId(), UserContextProvider.getCurrentTenantId());
        int modifiedCount = 0;
        switch (mongoCollectionEnum) {
            case FS_ENTRY:
                modifiedCount = fsEntryRepo.addTenantId();
                break;
            case HC_WORKSHEET:
                modifiedCount = hcWorksheetRepo.addTenantId();
                break;
            case MEMO_WORKSHEET:
                modifiedCount = memoWorksheetRepo.addTenantId();
                break;
            case OEM_FS_MAPPING:
                modifiedCount = oemFSMappingRepo.addTenantId();
                break;
            case ACCOUNTING_INFO:
                modifiedCount = accountingInfoRepo.addTenantId();
                break;
            case OEM_FINANCIAL_MAPPING:
                modifiedCount = oemFinancialMappingRepository.addTenantId();
                break;
            case OEM_FS_MAPPING_SNAPSHOT:
                modifiedCount = oemFsMappingSnapshotRepo.addTenantId();
                break;
            case OEM_FS_CELL_CODE_SNAPSHOT:
                modifiedCount = oemFsCellCodeSnapshotRepo.addTenantId();
                break;
            case OEM_FINANCIAL_MAPPING_MEDIA:
                modifiedCount = oemFinancialMappingMediaRepository.addTenantId();
                break;
            default:
                log.info("Mongo collection name  {} not matched with any bean", mongoCollectionEnum.name());
        }
        printLogsInfo(mongoCollectionEnum.name(), modifiedCount);
        return modifiedCount;
    }

    private void printLogsInfo(String collectionName, int updatedCount) {
        log.info("No of Documents updated for In {} for dealer {}, tenant {} is {}",
                collectionName,
                UserContextProvider.getCurrentDealerId(),
                UserContextProvider.getCurrentTenantId(),
                updatedCount);
    }
}
