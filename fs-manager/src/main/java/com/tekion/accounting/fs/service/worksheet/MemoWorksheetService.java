package com.tekion.accounting.fs.service.worksheet;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.dto.memo.CopyMemoValuesDto;
import com.tekion.accounting.fs.dto.memo.MemoBulkUpdateDto;
import com.tekion.accounting.fs.dto.memo.WorksheetRequestDto;
import com.tekion.accounting.fs.enums.OEM;

import java.util.List;
import java.util.Set;

public interface MemoWorksheetService {
	List<MemoWorksheet> getMemoWorksheet(String fsId);
	MemoWorksheet save(MemoWorksheet memoWorksheet);
	List<MemoWorksheet> bulkUpdate(MemoBulkUpdateDto memoBulkUpdateDtos);
	void migrateIfNotPresentFromTemplate(FSEntry fsEntry);
	List<MemoWorksheet> remigrateFromTemplate(String fsId);
	List<MemoWorksheet> migrateMemoWorksheetsForKeys(String fsId, Set<String> memoTemplateKeys);
	List<MemoWorksheet> migrateFieldTypeInMemoWorkSheet(String fsId, WorksheetRequestDto requestDto);
	List<MemoWorksheet> copyValues(CopyMemoValuesDto dto);
	List<MemoWorksheet> deleteMemoWorksheetsByKey(Set<String> keys, String fsId);
	List<MemoWorksheet> updateActiveFieldsFromPreviousWorksheets(OEM oem, int fromYear, int updatingYear, int version, String siteId);
	void migrateMemoWorksheetFromOemToFSLevel(String dealerId);
	List<MemoWorksheet> getMemoWorksheetsForExcel(String fsId,int month,boolean showEmptyValues);
}
