package com.tekion.accounting.fs.service.worksheet;

import com.google.common.collect.Lists;
import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.accounting.fs.beans.memo.MemoValue;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.dto.memo.CopyMemoValuesDto;
import com.tekion.accounting.fs.dto.memo.MemoBulkUpdateDto;
import com.tekion.accounting.fs.dto.memo.MemoWorkSheetUpdateDto;
import com.tekion.accounting.fs.dto.memo.WorksheetRequestDto;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.enums.OemCellDurationType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetTemplateRepo;
import com.tekion.accounting.fs.utils.DealerConfig;
import com.tekion.accounting.fs.utils.TimeUtils;
import com.tekion.accounting.fs.utils.UserContextUtils;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TPreConditions;
import com.tekion.core.utils.UserContextProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class MemoWorksheetServiceImpl implements MemoWorksheetService{
	private final MemoWorksheetTemplateRepo memoWorksheetTemplateRepo;
	private final MemoWorksheetRepo memoWorksheetRepo;
	private final FSEntryRepo fsEntryRepo;
	private final DealerConfig dealerConfig;

	@Override
	public List<MemoWorksheet> getMemoWorksheet(String fsId) {
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerId(fsId, UserContextProvider.getCurrentDealerId());
		if(Objects.isNull(fsEntry)){
			return null;
		}
		List<MemoWorksheet> memoWorksheets = memoWorksheetRepo.findByFSId(fsId);
		if(TCollectionUtils.isEmpty(memoWorksheets)){
			log.info("Memo work sheet not present fetching from template {} {} {}",fsEntry.getOemId(), fsEntry.getYear(), fsEntry.getVersion());
			memoWorksheets = migrateMemoWorksheetsFromTemplate(fsEntry);
		}
		return memoWorksheets;
	}


	public List<MemoWorksheet> getMemoWorksheetsForExcel(String fsId, int month_1_12, boolean keepZeroValues) {
		List<MemoWorksheet> memoWorksheets = getMemoWorksheet(fsId);
		if (keepZeroValues) return memoWorksheets;

		List<MemoWorksheet> finalMemoWorksheets = new ArrayList<>();

		List<MemoWorksheetTemplate> memoWorksheetTemplates = memoWorksheetTemplateRepo.findByOemYearAndCountry(memoWorksheets.get(0).getOemId(), memoWorksheets.get(0).getYear(), memoWorksheets.get(0).getVersion(), dealerConfig.getDealerCountryCode());
		Map<String, MemoWorksheetTemplate> map = new HashMap<>();
		for (MemoWorksheetTemplate memoWorksheetTemplate : memoWorksheetTemplates) {
			map.put(memoWorksheetTemplate.getKey(), memoWorksheetTemplate);
		}

		for (MemoWorksheet worksheet : memoWorksheets) {
			BigDecimal worksheetMtdValue, zeroValueForCompare, workSheetYtdValue;
			worksheetMtdValue = worksheet.getValues().get(month_1_12 - 1).getMtdValue();
			workSheetYtdValue = worksheet.getValues().get(month_1_12 - 1).getYtdValue();
			zeroValueForCompare = new BigDecimal(0);

			if (map.containsKey(worksheet.getKey())) {
				MemoWorksheetTemplate memoWorksheetTemplate = map.get(worksheet.getKey());
				if (memoWorksheetTemplate.getDurationTypes().contains(OemCellDurationType.YTD)) {
					if (zeroValueForCompare.compareTo(worksheetMtdValue) != 0 || zeroValueForCompare.compareTo(workSheetYtdValue) != 0) {
						finalMemoWorksheets.add(worksheet);
					}
				} else {
					if (zeroValueForCompare.compareTo(worksheetMtdValue) != 0) {
						finalMemoWorksheets.add(worksheet);
					}
				}
			}
		}
		return finalMemoWorksheets;
	}

	@Override
	public MemoWorksheet save(MemoWorksheet memoWorksheet) {
		MemoWorksheet saved = memoWorksheetRepo.findById(memoWorksheet.getId());
		TPreConditions.notNull(saved, "memoWorksheet.not.found");
		Collections.sort(memoWorksheet.getValues());
		saved.setValues(memoWorksheet.getValues());
		saved.setModifiedTime(System.currentTimeMillis());
		saved.setModifiedByUserId(UserContextProvider.getCurrentUserId());
		return memoWorksheetRepo.save(saved);
	}

	@Override
	public List<MemoWorksheet> bulkUpdate(MemoBulkUpdateDto bulkUpdateRequest) {
		if(TCollectionUtils.isEmpty(bulkUpdateRequest.getMemoWorksheets())){
			return Collections.EMPTY_LIST;
		}
		if(StringUtils.isEmpty(bulkUpdateRequest.getSiteId())){
			bulkUpdateRequest.setSiteId(UserContextUtils.getSiteIdFromUserContext());
		}
		Map<String, MemoWorkSheetUpdateDto> idToUpdateMap = TCollectionUtils.transformToMap(bulkUpdateRequest.getMemoWorksheets(), MemoWorkSheetUpdateDto::getId);
		List<MemoWorksheet> memoWorksheets = memoWorksheetRepo.findByIds(idToUpdateMap.keySet(),UserContextProvider.getCurrentDealerId());
		List<MemoWorksheet> toUpdate = Lists.newArrayList();
		TCollectionUtils.nullSafeList(memoWorksheets).forEach(memoWorksheet -> {
			if( Objects.nonNull(idToUpdateMap.get(memoWorksheet.getId()))){
				MemoWorkSheetUpdateDto updateDto = idToUpdateMap.get(memoWorksheet.getId());
				updateDto.getValues().forEach(memoValue -> {
					MemoValue value = memoWorksheet.getValues().get(memoValue.getMonth()-1);
					value.setMtdValue(memoValue.getMtdValue());
					value.setYtdValue(memoValue.getYtdValue());
				});
				memoWorksheet.setActive(updateDto.getActive());
				toUpdate.add(memoWorksheet);
			}
		});
		memoWorksheetRepo.updateBulk(toUpdate,UserContextProvider.getCurrentDealerId());
		return memoWorksheets;
	}

	@Override
	public List<MemoWorksheet> remigrateFromTemplate(String fsId) {
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
		log.info("Migrating the memo worksheet from template {} {} {}", fsEntry.getOemId(), fsEntry.getYear(), fsEntry.getVersion());
		memoWorksheetRepo.deleteWorkSheetsByFsId(fsId, UserContextProvider.getCurrentDealerId());
		return migrateMemoWorksheetsFromTemplate(fsEntry);
	}


	@Override
	public void migrateIfNotPresentFromTemplate(FSEntry fsEntry) {
		log.info("Migrating the memo worksheet from template {} {} {}", fsEntry.getOemId(), fsEntry.getYear(), fsEntry.getVersion());
		if(TCollectionUtils.isEmpty(memoWorksheetRepo.findByFSId(fsEntry.getId()))){
			log.info("creating for the first time : in CreationMode : {},{},{}",fsEntry.getOemId(),fsEntry.getYear(),fsEntry.getVersion());
			migrateMemoWorksheetsFromTemplate(fsEntry);
		}
	}

	@Override
	public List<MemoWorksheet> migrateMemoWorksheetsForKeys(String fsId, Set<String> memoTemplateKeys) {
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
		return migrateWorksheetsForSelectedMemoKeys(fsEntry, memoTemplateKeys);
	}

	@Override
	public List<MemoWorksheet> migrateMemoKeysForAllSites(OEM oemId, Integer year, Integer version, Set<String> memoTemplateKeys){
		List<FSEntry> fsEntries = fsEntryRepo.findByOemYearVersionAndSite(oemId.name(), year, version, UserContextProvider.getCurrentDealerId(), null);
		for(FSEntry fsEntry : TCollectionUtils.nullSafeList(fsEntries)){
			migrateWorksheetsForSelectedMemoKeys(fsEntry, memoTemplateKeys);
		}
		return null;
	}


	private List<MemoWorksheet> migrateMemoWorksheetsFromTemplate(FSEntry fsEntry) {
		List<MemoWorksheet> memoWorksheets;
		synchronized (this){
			memoWorksheets = TCollectionUtils.nullSafeList(memoWorksheetRepo.findByFSId(fsEntry.getId()));
			if(TCollectionUtils.isEmpty(memoWorksheets)){
				List<MemoWorksheetTemplate> memoWorksheetTemplates = memoWorksheetTemplateRepo.findByOemYearAndCountry(fsEntry.getOemId(), fsEntry.getYear(), fsEntry.getVersion(),
						dealerConfig.getDealerCountryCode());
				log.info("Migration memo worksheet from template {} {}",fsEntry.getYear(),fsEntry.getVersion());
				if(TCollectionUtils.isEmpty(memoWorksheetTemplates)) return null;
				memoWorksheetTemplates.forEach(memoWorksheetTemplate -> {
					memoWorksheets.add(covertToMemoWorksheet(memoWorksheetTemplate, fsEntry.getId()));
				});
				FSEntry previousYearEntry = fsEntryRepo.findDefaultTypeWithoutNullCheck(fsEntry.getOemId(),fsEntry.getYear()-1, UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
				if(Objects.nonNull(previousYearEntry)){
					updateActiveFieldsFromPreviousWorksheets(previousYearEntry, memoWorksheets);
				}
				memoWorksheetRepo.insertBulk(memoWorksheets);
			}
		}
		return memoWorksheets;
	}

	private List<MemoWorksheet> migrateWorksheetsForSelectedMemoKeys(FSEntry fsEntry, Set<String> memoKeysToMigrate) {
		Set<String> migratedKeys = memoWorksheetRepo.findByKeys(fsEntry.getId(), memoKeysToMigrate,
				UserContextProvider.getCurrentDealerId()).stream().map(MemoWorksheet::getKey).collect(Collectors.toSet());

		memoKeysToMigrate.removeAll(migratedKeys);
		if(TCollectionUtils.isEmpty(memoKeysToMigrate)) return Lists.newArrayList();

		List<MemoWorksheetTemplate> memoWorksheetTemplatesToMigrate = memoWorksheetTemplateRepo.findByOemYearAndCountry(fsEntry.getOemId(), fsEntry.getYear(), 1, memoKeysToMigrate, dealerConfig.getDealerCountryCode());

		List<MemoWorksheet> memoWorksheets = Lists.newArrayList();
		if(TCollectionUtils.isNotEmpty(memoWorksheetTemplatesToMigrate)){
			memoWorksheetTemplatesToMigrate.forEach(memoWorksheetTemplate -> {
				memoWorksheets.add(covertToMemoWorksheet(memoWorksheetTemplate, fsEntry.getId()));
			});
			memoWorksheetRepo.insertBulk(memoWorksheets);
		}
		return memoWorksheets;
	}

	private MemoWorksheet covertToMemoWorksheet(MemoWorksheetTemplate memoWorksheetTemplate, String fsId) {
		MemoWorksheet memoWorksheet = new MemoWorksheet();
		memoWorksheet.setFsId(fsId);
		memoWorksheet.setDealerId(UserContextProvider.getCurrentDealerId());
		memoWorksheet.setSiteId(UserContextUtils.getSiteIdFromUserContext());
		memoWorksheet.setKey(memoWorksheetTemplate.getKey());
//        memoWorksheet.setName(memoWorksheetTemplate.getName());
		memoWorksheet.setOemId(memoWorksheetTemplate.getOemId());
		memoWorksheet.setYear(memoWorksheetTemplate.getYear());
		memoWorksheet.setVersion(memoWorksheetTemplate.getVersion());
		memoWorksheet.setCreatedTime(System.currentTimeMillis());
		memoWorksheet.setModifiedTime(System.currentTimeMillis());
		memoWorksheet.setCreatedByUserId(UserContextProvider.getCurrentUserId());
		memoWorksheet.setModifiedByUserId(UserContextProvider.getCurrentUserId());
		memoWorksheet.setFieldType(memoWorksheetTemplate.getFieldType().toString());
//        memoWorksheet.setDurationTypes(memoWorksheetTemplate.getDurationTypes());
		List<MemoValue> memoValues = Lists.newArrayList();
		for(int i=1;i<=12;i++){
			MemoValue defaultValue = new MemoValue();
			defaultValue.setMonth(i);
			memoValues.add(defaultValue);
		}
		memoWorksheet.setValues(memoValues);
		return memoWorksheet;
	}

	@Override
	public List<MemoWorksheet> migrateFieldTypeInMemoWorkSheet(String fsId, WorksheetRequestDto requestDto) {

		List<MemoWorksheet> memoWorksheets = TCollectionUtils.nullSafeList(
				memoWorksheetRepo.findByFSId(fsId)
		);

		List<MemoWorksheetTemplate> memoWorksheetTemplates = memoWorksheetTemplateRepo.findByOemYearAndCountry(requestDto.getOemId().name(), requestDto.getYear(), requestDto.getVersion(), dealerConfig.getDealerCountryCode());

		Map<String, MemoWorksheet> memoMap = TCollectionUtils
				.transformToMap(TCollectionUtils.nullSafeList(memoWorksheets), MemoWorksheet::getKey);

		for(MemoWorksheetTemplate template: memoWorksheetTemplates){
			MemoWorksheet tempMemo = memoMap.get(template.getKey());
			if(tempMemo != null){
				tempMemo.setFieldType(template.getFieldType().name());
			}
		}

		memoWorksheetRepo.updateBulk(memoWorksheets, UserContextProvider.getCurrentDealerId());
		return memoWorksheets;
	}

	@Override
	public List<MemoWorksheet> deleteMemoWorksheetsByKey(OEM oem, int year, int version, Set<String> keys, String siteId) {
		if(TCollectionUtils.isEmpty(keys)){
			return Collections.emptyList();
		}
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oem.name(), year, UserContextProvider.getCurrentDealerId(), siteId);
		List<MemoWorksheet> memoWorksheetsToDelete = memoWorksheetRepo.findByKeys(fsEntry.getId(), keys, UserContextProvider.getCurrentDealerId());
		memoWorksheetRepo.deleteMemoWorksheetsByKeys(fsEntry.getId(), keys, UserContextProvider.getCurrentDealerId());
		return memoWorksheetsToDelete;
	}

	@Override
	public List<MemoWorksheet> copyValues(CopyMemoValuesDto dto) {
		//todo - need to handle for all fsEntries not for just default entry
		validate(dto);
		String oemId = dto.getOemId().name();
		int fromYear  = dto.getFromYear();
		int fromMonth = dto.getFromMonth();
		int toYear = dto.getToYear();
		int toMonth = dto.getToMonth();
		int version  = dto.getVersion();
		boolean copyForAllKeys = dto.isCopyAllValues();
		List<String> keys = dto.getKeys();

		FSEntry fsEntryFromYear = fsEntryRepo.findDefaultType(oemId, fromYear, UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		FSEntry fsEntryToYear = fsEntryRepo.findDefaultType(oemId, toYear, UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());

		if (TCollectionUtils.isEmpty(keys) && !copyForAllKeys) {
			return new ArrayList<>();
		}

		List<MemoWorksheetTemplate> templates = memoWorksheetTemplateRepo.findByOemYearAndCountry(oemId, toYear, version, dealerConfig.getDealerCountryCode());
		if(TCollectionUtils.isEmpty(templates)){
			throw new TBaseRuntimeException(String.format("Memo Templates are not present for year %d", toYear));
		}
		Map<String, MemoWorksheetTemplate> memoMap = templates.stream().collect(Collectors.toMap(MemoWorksheetTemplate::getKey, Function.identity()));


		List<MemoWorksheet> fromMemoSheets;
		if(copyForAllKeys){
			fromMemoSheets = memoWorksheetRepo.findByFSId(fsEntryFromYear.getId());
		}else{
			fromMemoSheets = memoWorksheetRepo.findByKeys(fsEntryFromYear.getId(), keys, UserContextProvider.getCurrentDealerId());
		}

		if (TCollectionUtils.isEmpty(fromMemoSheets)) {
			throw new TBaseRuntimeException("Memo worksheets for fromYear empty!");
		}

		List<MemoWorksheet> toMemoSheets;

		if (fromYear == toYear) {
			toMemoSheets = fromMemoSheets;
			copyFromMemoSheets(fromMemoSheets, toMemoSheets, fromMonth, toMonth, memoMap);
			memoWorksheetRepo.updateBulk(toMemoSheets, UserContextProvider.getCurrentDealerId());
			return toMemoSheets;
		}

		if(copyForAllKeys){
			toMemoSheets = memoWorksheetRepo.findByFSId(fsEntryToYear.getId());
		}else{
			toMemoSheets = TCollectionUtils.nullSafeList(memoWorksheetRepo
					.findByKeys(fsEntryToYear.getId(), keys, UserContextProvider.getCurrentDealerId()));
		}


		if (TCollectionUtils.isNotEmpty(toMemoSheets)) {
			copyFromMemoSheets(fromMemoSheets, toMemoSheets, fromMonth, toMonth, memoMap);
			memoWorksheetRepo.updateBulk(toMemoSheets, UserContextProvider.getCurrentDealerId());
			return toMemoSheets;
		}

		List<MemoWorksheet> finalToMemoSheets = new ArrayList<>();
		templates.forEach(memoWorksheetTemplate -> finalToMemoSheets.add(covertToMemoWorksheet(memoWorksheetTemplate, fsEntryToYear.getId())));
		toMemoSheets = finalToMemoSheets;

		copyFromMemoSheets(fromMemoSheets, toMemoSheets, fromMonth, toMonth, memoMap);
		memoWorksheetRepo.insertBulk(toMemoSheets);
		return toMemoSheets;
	}

	@Override
	public List<MemoWorksheet> updateActiveFieldsFromPreviousWorksheets(OEM oem, int fromYear, int updatingYear, int version, String siteId) {
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oem.name(), updatingYear, UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		List<MemoWorksheet> worksheetsToUpdate = memoWorksheetRepo.findByFSId(fsEntry.getId());

		if (TCollectionUtils.isEmpty(worksheetsToUpdate)) {
			throw new TBaseRuntimeException(AccountingError.memoWorksheetsAreMissing);
		}

		FSEntry previousYearEntry = fsEntryRepo.findDefaultType(oem.name(), fromYear, UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());

		updateActiveFieldsFromPreviousWorksheets(previousYearEntry, worksheetsToUpdate);
		memoWorksheetRepo.insertBulk(worksheetsToUpdate);
		return worksheetsToUpdate;
	}

	@Override
	public void migrateMemoWorksheetFromOemToFSLevel(String dealerId) {
		List<FSEntry> fsEntries = fsEntryRepo.getFSEntries(dealerId);
		for(FSEntry fsEntry : TCollectionUtils.nullSafeList(fsEntries)){
			memoWorksheetRepo.updateFsIdInMemoWorksheet(fsEntry);
		}
	}

	void updateActiveFieldsFromPreviousWorksheets(FSEntry fsEntry, List<MemoWorksheet> worksheetsToUpdate){
		List<MemoWorksheet> fromWorksheets = memoWorksheetRepo.findByFSId(fsEntry.getId());

		if (TCollectionUtils.isEmpty(fromWorksheets)) {
			log.error("Not migrating active fields, {} memo worksheets are missing", fsEntry.getId());
			return;
		}

		// there is a possibility of duplicate MemoWorksheets
		Map<String, MemoWorksheet> fromMap = fromWorksheets.stream().collect(Collectors.toMap(MemoWorksheet::getKey, y -> y, (a,b)-> a));
		Map<String, MemoWorksheet> toMap = worksheetsToUpdate.stream().collect(Collectors.toMap(MemoWorksheet::getKey, y -> y));

		for (String key : toMap.keySet()) {
			if (fromMap.containsKey(key)) {
				toMap.get(key).setActive(fromMap.get(key).getActive());
			}
		}
	}

	private void validate(CopyMemoValuesDto dto) {
		if( Objects.isNull(dto.getVersion())
				|| Objects.isNull(dto.getFromMonth())
				|| Objects.isNull(dto.getFromYear())
				|| Objects.isNull(dto.getToMonth())
				|| Objects.isNull(dto.getToYear())
				|| TCollectionUtils.isEmpty(dto.getKeys())
		){
			throw new TBaseRuntimeException(AccountingError.invalidPayload);
		}

		TimeUtils.validateTwelveIndexedMonth(dto.getFromMonth());
		TimeUtils.validateTwelveIndexedMonth(dto.getToMonth());
	}

	private void copyFromMemoSheets(List<MemoWorksheet> fromMemoSheets, List<MemoWorksheet> toMemoSheets
			, int fromMonth, int toMonth, Map<String, MemoWorksheetTemplate> memoMap){

		Map<String, MemoWorksheet> fromSheetMap = TCollectionUtils.transformToMap(fromMemoSheets, MemoWorksheet::getKey);
		Map<String, MemoWorksheet> toSheetMap = TCollectionUtils.transformToMap(toMemoSheets, MemoWorksheet::getKey);

		for(String key: fromSheetMap.keySet()){

			Map<Integer, MemoValue> toMemoValueMap = toSheetMap.get(key).getValues().stream()
					.collect(Collectors.toMap(MemoValue::getMonth, Function.identity()));

			Map<Integer, MemoValue> fromMemoValueMap = fromSheetMap.get(key).getValues().stream()
					.collect(Collectors.toMap(MemoValue::getMonth, Function.identity()));

			MemoValue valueTo = toMemoValueMap.get(toMonth);
			MemoValue valueFrom = fromMemoValueMap.get(fromMonth);

			BigDecimal diff = valueFrom.getMtdValue().subtract(valueTo.getMtdValue());
			valueTo.setMtdValue(valueFrom.getMtdValue());

			if(TCollectionUtils.nullSafeCollection(memoMap.get(key).getDurationTypes()).contains(OemCellDurationType.YTD)){
				for(int i=toMonth; i <= 12; i++){
					BigDecimal newYtd = toMemoValueMap.get(i).getYtdValue().add(diff);
					toMemoValueMap.get(i).setYtdValue(newYtd);
				}
			}
		}
	}
}
