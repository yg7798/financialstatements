package com.tekion.accounting.fs.service.worksheet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.dto.memo.MemoWorksheetTemplateRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetTemplateRepo;
import com.tekion.accounting.fs.service.utils.FSLocaleUtils;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.multilingual.commons.service.dbtype.DBTypeMultiLingualService;
import com.tekion.multilingual.dto.MultiLingualExportRequest;
import com.tekion.multilingual.dto.MultiLingualImportRequest;
import com.tekion.multilingual.dto.Record;
import com.tekion.multilingual.dto.TekMultiLingualBean;
import com.tekion.multilingual.dto.export.ExportResponse;
import com.tekion.multilingual.dto.importml.ImportResponse;
import com.tekion.tekionconstant.locale.TekLocale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoWorksheetTemplateServiceImpl implements MemoWorksheetTemplateService, DBTypeMultiLingualService {
	public static final String MEMO_MULTILINGUAL_ASSET_NAME = "MemoWorksheet";
	private final MemoWorksheetTemplateRepo memoWorksheetTemplateRepo;
	private final DealerConfig dealerConfig;

	@Override
	public List<MemoWorksheetTemplate> getMemoWorksheetTemplates(OEM oemId, int year, int version) {
		List<MemoWorksheetTemplate> templates = memoWorksheetTemplateRepo.findByOemYearAndCountry(oemId.name(), year, version, dealerConfig.getDealerCountryCode());
		translateValuesForName(templates);
		return templates;
	}

	@Override
	public void save(MemoWorksheetTemplateRequestDto memoWorksheetRequest) {
		MemoWorksheetTemplate memoWorksheetTemplate = memoWorksheetRequest.toMemoWorksheetTemplate();
		memoWorksheetTemplateRepo.save(memoWorksheetTemplate);
	}

	@Override
	public void saveBulk(List<MemoWorksheetTemplateRequestDto> memoWorksheetRequests) {
		List<MemoWorksheetTemplate> memoWorksheetTemplates = Lists.newArrayList();
		TCollectionUtils.nullSafeList(memoWorksheetRequests).forEach(memoWorksheetRequestDto -> {
			memoWorksheetTemplates.add(memoWorksheetRequestDto.toMemoWorksheetTemplate());
		});
		if(TCollectionUtils.isNotEmpty(memoWorksheetRequests)){
			memoWorksheetTemplateRepo.updateBulk(memoWorksheetTemplates);
		}
	}

	@Override
	public List<MemoWorksheetTemplate> deleteMemoWorksheetTemplatesByKeys(OEM oemId, int year, int version, Set<String> keys, String country) {
		if(TCollectionUtils.isEmpty(keys)){
			return Collections.emptyList();
		}
		List<MemoWorksheetTemplate> memoTemplatesToDelete = memoWorksheetTemplateRepo.findByOemYearAndCountry(oemId.name(), year, version, keys, country);
		memoWorksheetTemplateRepo.deleteTemplatesByKey(oemId.name(), year, version, keys, country);
		return memoTemplatesToDelete;
	}

	@Override
	public void deleteMWTemplatesByOemByCountryByYear(OEM oemId, Integer year, String countryCode) {
		memoWorksheetTemplateRepo.deleteMWTemplatesByOemByCountryByYear(oemId, year, countryCode);
	}

	@Override
	public void deleteMWTemplatesByOemByCountryByYearByKeys(OEM oemId, Integer year, Set<String> keys, String countryCode) {
		if(TCollectionUtils.isEmpty(keys))
			return;
		memoWorksheetTemplateRepo.deleteMWTemplatesByOemByCountryByYearByKeys(oemId, year, keys, countryCode);
	}

	private void translateValuesForName(List<MemoWorksheetTemplate> templates) {
		templates.forEach(template -> template.setName(FSLocaleUtils.getTranslatedValue(template.getLanguages(), MemoWorksheetTemplate.NAME, template.getName())));
	}

	@Override
	public ExportResponse exportMultilingualRecords(MultiLingualExportRequest request) {
		log.info("exportMultilingualRecords request received {}", request);
		List<MemoWorksheetTemplate> response = memoWorksheetTemplateRepo.findBySortByIdAndPageToken(request.getNextPageToken(), request.getBatchSize());
		ExportResponse exportResponse = transformToExportResponse(response);
		log.info("MultiLingualExportRecords request is {}, ExportResponse is {}", request, exportResponse);
		return exportResponse;
	}

	@Override
	public ImportResponse importMultilingualRecords(MultiLingualImportRequest request) {
		log.info("importMultilingualRecords request received {}", request);
		if(request.getAssetName().equalsIgnoreCase(getMultilingualAssetName())){
			Map<String, TekMultiLingualBean> keyToValueMap = Maps.newHashMap();
			request.getRecords().forEach(record -> {
				keyToValueMap.put(record.getId(), record.getTekMultiLingualBean());
			});
			memoWorksheetTemplateRepo.languagesBulkUpdate(keyToValueMap);
		}
		return new ImportResponse();
	}

	@Override
	public String getMultilingualAssetName() {
		return MEMO_MULTILINGUAL_ASSET_NAME;
	}

	private ExportResponse transformToExportResponse(List<MemoWorksheetTemplate> templates) {
		ExportResponse exportResponse = new ExportResponse();
		exportResponse.setAssetName(getMultilingualAssetName());
		if(!templates.isEmpty())
			exportResponse.setNextPageToken(templates.get(templates.size()-1).getId());

		List<Record> records = new ArrayList<>();
		templates.forEach(template -> {
			Record record = new Record();
			record.setId(template.getId());
			record.setDealerId(TConstants.ZERO_STRING);
			record.setTenantId(TConstants.ZERO_STRING);
			TekMultiLingualBean languages = template.getLanguages();
			if (languages == null || TCollectionUtils.isEmpty(languages.getLocale())) {
				Map<TekLocale, Map<String, Object>> locale = new HashMap<>();

				Map<String, Object> keyToValueMap = new HashMap<>();
				keyToValueMap.put(MemoWorksheetTemplate.NAME, template.getName());
				locale.put(TekLocale.en, keyToValueMap);
				locale.put(TekLocale.en_US, keyToValueMap);

				languages = new TekMultiLingualBean();
				languages.setLocale(locale);
				record.setTekMultiLingualBean(languages);
			}
			records.add(record);
		});
		exportResponse.setRecords(records);
		return exportResponse;
	}
}

