package com.tekion.accounting.fs.service.worksheet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.memo.HCDepartment;
import com.tekion.accounting.fs.beans.memo.HCValue;
import com.tekion.accounting.fs.beans.memo.HCWorksheet;
import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;
import com.tekion.accounting.fs.dto.memo.CopyHCWorksheetValuesDto;
import com.tekion.accounting.fs.dto.memo.HCBulkUpdateDto;
import com.tekion.accounting.fs.dto.memo.HCUpdateDto;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetTemplateRepo;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class HCWorksheetServiceImpl implements HCWorksheetService{
	private final HCWorksheetTemplateRepo hcWorksheetTemplateRepo;
	private final HCWorksheetRepo hcWorksheetRepo;
	private final FSEntryRepo fsEntryRepo;
	private final DealerConfig dealerConfig;

	@Override
	public HCWorksheetTemplate getHCWorksheetTemplate(OEM oemId, int year, int version) {
		return hcWorksheetTemplateRepo.findForOemByYearAndCountry(oemId.name(),year,version, dealerConfig.getDealerCountryCode());
	}

	@Override
	public List<HCWorksheet> getHCWorksheets(String fsId) {
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
		List<HCWorksheet> worksheets =  hcWorksheetRepo.findByFsId(fsId);
		if(TCollectionUtils.isEmpty(worksheets)){
			log.info("HCWorksheets are empty, so migrating from hcTemplate!");
			return migrateFromTemplateWithFsId(OEM.valueOf(fsEntry.getOemId()), fsEntry.getYear(), fsEntry.getVersion(), fsId);
		}
		return worksheets;
	}

	@Override
	public void upsertBulk(List<HCWorksheetTemplate> hcWorksheetTemplates) {
		hcWorksheetTemplateRepo.upsertBulk(hcWorksheetTemplates);
	}

	@Override
	public HCWorksheetTemplate save(HCWorksheetTemplate hcWorksheetTemplate) {
		HCWorksheetTemplate saved =   hcWorksheetTemplateRepo.findForOemByYearAndCountry(hcWorksheetTemplate.getOemId(),hcWorksheetTemplate.getYear(),
				hcWorksheetTemplate.getVersion(), hcWorksheetTemplate.getCountry());
		if(Objects.isNull(saved)){
			hcWorksheetTemplate.setCreatedTime(System.currentTimeMillis());
		}else {
			saved.setPositions(hcWorksheetTemplate.getPositions());
			saved.setDepartments(hcWorksheetTemplate.getDepartments());
			saved.setAdditionalInfo(hcWorksheetTemplate.getAdditionalInfo());
			hcWorksheetTemplate = saved;
		}
		hcWorksheetTemplate.setModifiedTime(System.currentTimeMillis());
		return hcWorksheetTemplateRepo.save(hcWorksheetTemplate);
	}

	@Override
	public List<HCWorksheet> bulkUpdate(HCBulkUpdateDto hcBulkUpdateDto) {
		if(StringUtils.isEmpty(hcBulkUpdateDto.getSiteId())){
			hcBulkUpdateDto.setSiteId(UserContextUtils.getSiteIdFromUserContext());
		}
		Map<String, HCUpdateDto> hcUpdateDtoMap = Maps.newHashMap();
		List<String> ids = Lists.newArrayList();
		for(HCUpdateDto hcUpdateDto: hcBulkUpdateDto.getHcWorksheets()){
			ids.add(hcUpdateDto.getId());
			hcUpdateDtoMap.put(hcUpdateDto.getId(),hcUpdateDto);
		}
		List<HCWorksheet> toUpdate = hcWorksheetRepo.findByIds(ids, UserContextProvider.getCurrentDealerId());
		TCollectionUtils.nullSafeList(toUpdate).forEach(hcWorksheet -> {
			if(hcUpdateDtoMap.containsKey(hcWorksheet.getId())){
				Map<Integer, HCValue> monthToValueMap = TCollectionUtils.transformToMap(hcWorksheet.getValues(),HCValue::getMonth);
				for(HCValue hcValue: hcUpdateDtoMap.get(hcWorksheet.getId()).getValues()){
					if(Objects.nonNull(hcValue) && monthToValueMap.containsKey(hcValue.getMonth())){
						monthToValueMap.get(hcValue.getMonth()).setValue(hcValue.getValue());
					}
				}
			}
		});
		hcWorksheetRepo.updateBulk(toUpdate, UserContextProvider.getCurrentDealerId());
		return toUpdate;
	}

	@Override
	public synchronized List<HCWorksheet> migrateFromTemplate(OEM oemId, int year, int version) {
		HCWorksheetTemplate hcWorksheetTemplate = hcWorksheetTemplateRepo.findForOemByYearAndCountry(oemId.name(),year,version, dealerConfig.getDealerCountryCode());
		if (Objects.isNull(hcWorksheetTemplate)) {
			log.error("cannot migrate, hcWorksheetTemplate not found for {} {} {}", oemId, year, version);
			return new ArrayList<>();
		}
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oemId.name(), year,UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		List<HCWorksheet> hcWorksheets = getWorksheetsToInsert(hcWorksheetTemplate, fsEntry);
		hcWorksheetRepo.insertBulk(hcWorksheets);
		return hcWorksheets;
	}

	@Override
	public synchronized List<HCWorksheet> migrateFromTemplateWithFsId(OEM oemId, int year, int version, String fsId) {
		HCWorksheetTemplate hcWorksheetTemplate = hcWorksheetTemplateRepo.findForOemByYearAndCountry(oemId.name(),year,version, dealerConfig.getDealerCountryCode());
		if (Objects.isNull(hcWorksheetTemplate)) {
			log.error("cannot migrate, hcWorksheetTemplate not found for {} {} {}", oemId, year, version);
			return new ArrayList<>();
		}
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
		List<HCWorksheet> hcWorksheets = getWorksheetsToInsert(hcWorksheetTemplate, fsEntry);
		hcWorksheetRepo.insertBulk(hcWorksheets);
		return hcWorksheets;
	}

	@Override
	public List<HCWorksheet> copyValues(CopyHCWorksheetValuesDto dto) {
		validate(dto);

		String oemId = dto.getOemId().name();
		int fromYear = dto.getFromYear();
		int fromMonth = dto.getFromMonth();
		int toYear = dto.getToYear();
		int toMonth = dto.getToMonth();
		int version = dto.getVersion();

		FSEntry fsEntryFromYear = fsEntryRepo.findDefaultType(oemId, fromYear,UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		FSEntry fsEntryToYear = fsEntryRepo.findDefaultType(oemId, toYear,UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());

		List<HCWorksheet> fromWorksheets = hcWorksheetRepo.findByFsId(fsEntryFromYear.getId());
		if (TCollectionUtils.isEmpty(fromWorksheets)) {
			throw new TBaseRuntimeException("Headcount worksheet is missing for fromYear!");
		}

		List<HCWorksheet> toWorksheets = fromWorksheets;

		if (fromYear == toYear) {
			copyValues(fromWorksheets, toWorksheets, fromMonth, toMonth);
			hcWorksheetRepo.updateBulk(toWorksheets, UserContextProvider.getCurrentDealerId());
			return toWorksheets;
		}

		toWorksheets = hcWorksheetRepo.findByFsId(fsEntryToYear.getId());

		if (TCollectionUtils.isNotEmpty(toWorksheets)) {
			copyValues(fromWorksheets, toWorksheets, fromMonth, toMonth);
			hcWorksheetRepo.updateBulk(toWorksheets, UserContextProvider.getCurrentDealerId());
			return toWorksheets;
		}

		HCWorksheetTemplate hcWorksheetTemplate = hcWorksheetTemplateRepo.findForOemByYearAndCountry(oemId, toYear, version, dealerConfig.getDealerCountryCode());

		if (Objects.isNull(hcWorksheetTemplate)) {
			throw new TBaseRuntimeException("HCTemplate and worksheet both are missing");
		}

		toWorksheets = getWorksheetsToInsert(hcWorksheetTemplate, fsEntryToYear);

		copyValues(fromWorksheets, toWorksheets, fromMonth, toMonth);
		hcWorksheetRepo.insertBulk(toWorksheets);
		return toWorksheets;
	}

	@Override
	public void migrateHeadCountWorksheetFromOemToFSLevel(String dealerId) {
		List<FSEntry> fsEntries = fsEntryRepo.getFSEntries(dealerId);
		for(FSEntry fsEntry : TCollectionUtils.nullSafeList(fsEntries)){
			hcWorksheetRepo.updateFsIdInHCWorksheets(fsEntry);
		}
	}

	private void validate(CopyHCWorksheetValuesDto dto) {
		if( Objects.isNull(dto.getVersion())
				|| Objects.isNull(dto.getFromMonth())
				|| Objects.isNull(dto.getFromYear())
				|| Objects.isNull(dto.getToMonth())
				|| Objects.isNull(dto.getToYear())
		){
			throw new TBaseRuntimeException(AccountingError.invalidPayload);
		}

		TimeUtils.validateTwelveIndexedMonth(dto.getFromMonth());
		TimeUtils.validateTwelveIndexedMonth(dto.getToMonth());
	}

	private void copyValues(List<HCWorksheet> fromSheets, List<HCWorksheet> toSheets, int fromMonth, int toMonth){
		Map<String, Map<String, HCWorksheet>> fromSheetsMap = new HashMap<>();

		for(HCWorksheet sheet: toSheets){
			fromSheetsMap.computeIfAbsent(sheet.getDepartment(), k -> new HashMap<>()).put(sheet.getPosition(), sheet);
		}

		for(HCWorksheet fromSheet: fromSheets){
			HCWorksheet toSheet = fromSheetsMap.get(fromSheet.getDepartment()).get(fromSheet.getPosition());
			Map<Integer, HCValue> toMap = toSheet.getValues().stream().collect(Collectors.toMap(HCValue::getMonth, Function.identity()));
			Map<Integer, HCValue> fromMap = toSheet.getValues().stream().collect(Collectors.toMap(HCValue::getMonth, Function.identity()));

			HCValue valueToSet = toMap.get(toMonth);
			HCValue valueToPick = fromMap.get(fromMonth);
			valueToSet.setValue(valueToPick.getValue());
		}
	}

	private List<HCWorksheet> getWorksheetsToInsert(HCWorksheetTemplate hcWorksheetTemplate, FSEntry fsEntry){
		String fsId = fsEntry.getId();
		List<HCWorksheet> hcWorksheets = new ArrayList<>();
		for(HCDepartment hcDepartment : hcWorksheetTemplate.getDepartments()){
			hcWorksheets.addAll(createHCWorksheetForDepartment(fsEntry.getOemId(), hcWorksheetTemplate.getYear(), fsEntry.getVersion(), hcDepartment, fsId));
		}

		return hcWorksheets;
	}

	private Collection<? extends HCWorksheet> createHCWorksheetForDepartment(String oemId, int year, int version, HCDepartment hcDepartment, String fsId) {
		List<HCWorksheet> hcWorksheets = Lists.newArrayList();

		for(String position: hcDepartment.getSupportedPositions()){
			HCWorksheet hcWorksheet = new HCWorksheet();
			hcWorksheet.setFsId(fsId);
			hcWorksheet.setCreatedByUserId(UserContextProvider.getCurrentUserId());
			hcWorksheet.setDealerId(UserContextProvider.getCurrentDealerId());
			hcWorksheet.setSiteId(UserContextUtils.getSiteIdFromUserContext());
			hcWorksheet.setDepartment(hcDepartment.getKey());
			hcWorksheet.setPosition(position);
			hcWorksheet.setOemId(oemId);
			hcWorksheet.setYear(year);
			hcWorksheet.setVersion(version);
			long time = System.currentTimeMillis();
			hcWorksheet.setCreatedTime(time);
			hcWorksheet.setModifiedTime(time);
			hcWorksheet.setModifiedByUserId(UserContextProvider.getCurrentUserId());
			hcWorksheet.setValues(Lists.newArrayList());
			for(int month=1;month<=12;month++){
				HCValue value = new HCValue();
				value.setMonth(month);
				value.setValue(BigDecimal.ZERO);
				hcWorksheet.getValues().add(value);
			}
			hcWorksheets.add(hcWorksheet);
		}
		return hcWorksheets;
	}
}

