package com.tekion.accounting.fs.service.compute;

import com.tekion.accounting.fs.beans.common.*;
import com.tekion.accounting.fs.dto.cellGrouop.FSCellGroupCodeCreateDto;
import com.tekion.accounting.fs.dto.cellGrouop.FSCellGroupCodesCreateDto;
import com.tekion.accounting.fs.dto.cellGrouop.FsGroupCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.cellcode.*;
import com.tekion.accounting.fs.dto.mappings.*;
import com.tekion.accounting.fs.dto.oemConfig.OemConfigRequestDto;
import com.tekion.accounting.fs.dto.oemTemplate.OemTemplateReqDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.as.models.dto.MonthInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FsComputeService {

	void saveMapping(OemMappingRequestDto mappingRequest);

	OEMMappingResponse getOEMMappingByDealerId(OEM oem, String year, String dealerId);

	List<AccountingOemFsCellCode> getOemTMappingList(String oemId, Integer year, Integer version, String countryCode, boolean readFromCache);

	List<AccountingOemFsCellCode> getOemTMappingList(String oemId, Integer year, Integer version, String countryCode);

	AccountingOemFsCellCode saveFsCellCode(FSCellCodeCreateDto reqDto);

	List<AccountingOemFsCellCode> saveFsCellCodes(FSCellCodeListCreateDto reqDto);

	FsCellCodeDetailsResponseDto computeFsCellCodeDetails(String oemId, Integer oemFsYear, Integer oemFsVersion, Integer year, Integer month, boolean includeM13, String siteId, boolean addM13BalInDecBalances);

	FsCellCodeDetailsResponseDto computeFsCellCodeDetailsByFsId(String fsId, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances);

	FsCellCodeDetailsResponseDto computeFsCellCodeDetails(FSEntry fsEntry, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances);

	FsGroupCodeDetailsResponseDto computeFsGroupCodeDetails(String oemId, Integer oemFsYear, Integer oemFsVersion, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances, String siteId);

	FsCellCodeDetailsResponseDto computeFsCellCodeDetailsForFS(String fsId, long tillEpoch, boolean includeM13);

	List<OEMFsCellCodeSnapshotResponseDto> getFsCellCodeDetails(String oemId, Integer oemFsYear, Integer oemFsVersion,
																long tillEpoch, Set<String> codes, boolean includeM13, boolean addM13BalInDecBalances);

	void saveTemplate(OemTemplateReqDto reqDto);

	List<OemTemplate> getOemTemplate(String oemId, Integer year);

	AccountingOemFsCellGroup saveFsCellGroupCode(FSCellGroupCodeCreateDto reqDto);

	List<AccountingOemFsCellGroup> fetchFsCellGroupCodes(String oemId, Integer year, Integer version);

	List<AccountingOemFsCellGroup> fetchFsCellGroupCodesInBulk(Integer year, Set<OEM> oemIds);

	void saveFsCellGroupCodes(FSCellGroupCodesCreateDto reqDto);

	void upsertFsCellGroupCodes(FSCellGroupCodesCreateDto reqDto);

	void migrateFsCellCodesFromGroup(OEM oem);

	void populateGroupCodesInFsCell(OEM oem, int year, int version);

	void createFsCellCodeSnapshotForYearAndMonth(CellCodeSnapshotCreateDto dto);

	List<OEMFsCellCodeSnapshot> getFsCellCodeSnapshots(String fsId, Integer month_1_12);

	void createFsCellCodeSnapshot(FSEntry fsEntry, int year, int month, boolean includeM13, boolean addM13BalInDecBalances);

	List<OEMFsCellCodeSnapshotResponseDto> getOEMFsCellCodeSnapshot(String siteId, String oemId, int oemFsVersion, int year, int month,
																	Set<String> codes, int oemFsYear, boolean includeM13, boolean addM13BalInDecBalances);

	void createBulkFsCellCodeSnapshot(String siteId, String oemId, int oemFsYear, int oemFsVersion, int year, int fromMonth, int toMonth, boolean includeM13, boolean addM13BalInDecBalances);

	boolean deleteSnapshotByYearAndMonth(String siteId,String oemId, int oemFsVersion, int year, int month);

	boolean deleteSnapshotsInBulk(FSCellCodeSnapshotDto dto);

	List<OEMFsCellCodeSnapshotResponseDto> getAllOEMFsCellCodeSnapshotSummary(String siteId, String oemId, int oemFsVersion, int year, int month,
																			  int oemFsYear, boolean includeM13, boolean addM13BalInDecBalances);

	List<OEMFsCellCodeSnapshotBulkResponseDto> getBulkOEMFsCellCodeSnapshot(String oemId, Set<String> codes, long fromTimestamp, long toTimestamp,
																			int oemFsVersion, int oemFsYear, boolean includeM13, String siteId, boolean addM13BalInDecBalances);

	void createFsMappingSnapshotBulk(String siteId,String oemId, int oemFsYear, int oemFsVersion, int year, int fromMonth, int toMonth);

	void createFsMappingSnapshot(String fsId, int month);

	void createFsMappingAndCellCodeSnapshot(int year, int month, boolean includeM13, String siteId, boolean addM13BalInDecBalances);

	void createFsMappingAndCellCodeSnapshotForAllSites(int year, int month, boolean includeM13, boolean addM13BalInDecBalances);

	boolean deleteBulkSnapshotByYearAndMonth(String siteId, String oemId, int year, int fromMonth, int toMonth);

	OemCodeUpdateDto updateOemCode(OemCodeUpdateDto oemCodeUpdateDtos);

	OemConfig getOemConfig(String oemId);

	OemConfig saveOemConfig(OemConfigRequestDto requestDto);

	void invalidateCache();

	List<AccountingOemFsCellCode> deleteCellCodes(FsCellCodeDeleteDto dto);

	List<AccountingOemFsCellCode> migrateCellCodesToYear(String oemId, int fromYear, int toYear, String country);

	List<AccountingOemFsCellGroup> migrateGroupsCodesToYear(String oemId, int fromYear, int toYear, String country);

	void createSnapshotsForMapping(MappingSnapshotDto dto);

	void deleteMappingSnapshots(MappingSnapshotDto dto);

	List<AccountingOemFsCellGroup> deleteGroupCodes(String oemId, int year, List<String> groupCodes, int version, String country);

	Map<Integer, Map<String, Set<String>>> getDependentGlAccounts(OEM oem, int year, int version, Set<String> cellCodes, Long epoch, String siteId);

	FsCellCodeDetailsResponseDto computeFsDetails(String oemId, Integer oemFsYear, Integer oemFsVersion, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances);

	FSEntry createFsmInfoIfPresent(OEM oem, int year, int version);

	boolean isFutureMonth(MonthInfo activeMonthInfo, int year, int month);

	List<FSEntry> updateSiteIdInOemMappings(List<OemFsMappingSiteIdChangesReqDto> reqDtos);

	void migrateOemFsMappingFromOemToFSLevel(String dealerId);

	void migrateOemFsCellCodeSnapshotsFromOemToFSLevel(String dealerId);

	void migrateOemFsMappingSnapshotsFromOemToFSLevel(String dealerId);

	void addFsTypeInOemFsCellCodeSnapshots(String dealerId);
}

