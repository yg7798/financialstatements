package com.tekion.accounting.fs.service.fsEntry;

import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.accounting.fs.dto.fsEntry.FSEntryUpdateDto;
import com.tekion.accounting.fs.dto.fsEntry.FsEntryCreateDto;
import com.tekion.accounting.fs.dto.mappings.FsMappingInfosResponseDto;
import com.tekion.admin.beans.dealersetting.DealerMaster;

import java.util.List;

public interface FsEntryService {

  FSEntry createFSEntry(FsEntryCreateDto reqDto);

  FsMappingInfosResponseDto getAllFSEntries();

  FsMappingInfosResponseDto getFSEntry(String oemId);

  FSEntry getFSEntryById(String id);

  List<FSEntry> findFsEntriesForYear(String siteId, Integer year);

  FSEntry updateFSEntry(String fsMappingId, FSEntryUpdateDto FSEntryUpdateDto);

  FSEntry deleteFsEntryById(String fsIdsToDelete);

  List<FSEntry> findFsEntriesForYear(Integer year);

  List<DealerMaster> getDealersDetailForConsolidatedFS(String fsId);

  FsMappingInfosResponseDto getFSEntriesBySiteId(List<String> siteIds);

}
