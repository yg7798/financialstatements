package com.tekion.accounting.fs.service.migration;

public interface FSMigrationService {

    void setSiteIdInOemFinancialMapping();

    void setSiteIdInOemFsMapping();

    void setSiteIdInOemFsMappingInfo();

    void setSiteIdInOEMFsCellCodeSnapshot();

    void setSiteIdInOemFsMappingSnapshot();

    void setSiteIdInMemoWorksheet();

    void setSiteIdInHeadCountWorksheet();

    void setFsTypeInFsEntries();

    void migrateOemFsMappingFromOemToFSLevel();

    void migrateMemoWorksheetFromOemToFSLevel();

    void migrateHeadCountWorksheetFromOemToFSLevel();

    void migrateOemFsCellCodeSnapshotsFromOemToFSLevel();

    void migrateOemFsMappingSnapshotsFromOemToFSLevel();

    void addFsTypeInOemFsCellCodeSnapshots();

    void addCountryInOemTemplate();

    void addCountryInMemoWorksheetTemplate();

    void addCountryInHeadCountWorksheetTemplate();

    void addCountryInOemFsCellCodes();

    void addCountryInOemFsCellGroupCodes();

    void addCountryInOemConfigs();

    void addCountryInFsExcelMetaDataMappingInfo();
}
