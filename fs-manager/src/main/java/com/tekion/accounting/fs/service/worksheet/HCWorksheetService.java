package com.tekion.accounting.fs.service.worksheet;

import com.tekion.accounting.fs.beans.memo.HCWorksheet;
import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;
import com.tekion.accounting.fs.dto.memo.CopyHCWorksheetValuesDto;
import com.tekion.accounting.fs.dto.memo.HCBulkUpdateDto;
import com.tekion.accounting.fs.enums.OEM;

import java.util.List;

public interface HCWorksheetService {
	HCWorksheetTemplate getHCWorksheetTemplate(OEM oemId, int year, int version);
	HCWorksheetTemplate save(HCWorksheetTemplate hcWorksheetTemplate);
	void upsertBulk(List<HCWorksheetTemplate> hcWorksheetTemplates);
	List<HCWorksheet> migrateFromTemplate(OEM oemId, int year, int version);
	List<HCWorksheet> migrateFromTemplateWithFsId(OEM oemId, int year, int version, String fsId);
	List<HCWorksheet> bulkUpdate(HCBulkUpdateDto hcBulkUpdateDto);
	List<HCWorksheet> getHCWorksheets(String fsId);
	List<HCWorksheet> copyValues(CopyHCWorksheetValuesDto dto);
	void  migrateHeadCountWorksheetFromOemToFSLevel(String dealerId);
}
