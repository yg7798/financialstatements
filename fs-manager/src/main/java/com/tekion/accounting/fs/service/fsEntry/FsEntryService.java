package com.tekion.accounting.fs.service.fsEntry;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.dto.fsEntry.FSEntryUpdateDto;
import com.tekion.accounting.fs.dto.fsEntry.FsEntryCreateDto;
import com.tekion.accounting.fs.dto.mappings.FsMappingInfosResponseDto;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;

import java.util.List;

public interface FsEntryService {

  FSEntry createFSEntry(FsEntryCreateDto reqDto);

  FsMappingInfosResponseDto getAllFSEntries();

  List<FSEntry> getFSEntries();

  FsMappingInfosResponseDto getFSEntry(String oemId, String siteId);

  FSEntry getFSEntryById(String id);

  List<FSEntry> findFsEntriesForYear(String siteId, Integer year);

  FSEntry updateFSEntry(FSEntryUpdateDto FSEntryUpdateDto);

  FSEntry deleteFsEntryById(String fsIdsToDelete);

  List<FSEntry> findFsEntriesForYear(Integer year);

  List<DealerMaster> getDealersDetailForConsolidatedFS(String fsId);

  FsMappingInfosResponseDto getFSEntriesBySiteId(List<String> siteIds);


  void migrateFsEntriesFromYearToYear(Integer fromYear, Integer toYear);

  void migrateFSName();

  void migrateParentRef(Integer year);

  FSEntry updateSiteId(String fsId, String siteId);

  Long updateFsTypeForFsEntry(String fsId, FSType changedType);

}
