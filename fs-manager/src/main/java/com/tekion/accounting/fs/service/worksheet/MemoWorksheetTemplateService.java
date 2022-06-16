package com.tekion.accounting.fs.service.worksheet;

import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.dto.memo.MemoWorksheetTemplateRequestDto;
import com.tekion.accounting.fs.enums.OEM;

import java.util.List;
import java.util.Set;

public interface MemoWorksheetTemplateService {
	List<MemoWorksheetTemplate> getMemoWorksheetTemplates(OEM oemId, int year, int version);
	void save(MemoWorksheetTemplateRequestDto memoWorksheetRequest);
	void saveBulk(List<MemoWorksheetTemplateRequestDto> memoWorksheetRequests);
	List<MemoWorksheetTemplate> deleteMemoWorksheetTemplatesByKeys(OEM oemId, int year, int version, Set<String> keys, String country);

	void deleteMWTemplatesByOemByCountryByYear(OEM oemId, Integer year, String countryCode);

	void deleteMWTemplatesByOemByCountryByYearByKeys(OEM oemId, Integer year, Set<String> keys, String countryCode);
}
