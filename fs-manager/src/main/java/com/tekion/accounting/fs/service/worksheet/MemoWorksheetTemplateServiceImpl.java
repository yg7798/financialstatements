package com.tekion.accounting.fs.service.worksheet;

import com.google.common.collect.Lists;
import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.dto.memo.MemoWorksheetTemplateRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetTemplateRepo;
import com.tekion.core.utils.TCollectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MemoWorksheetTemplateServiceImpl implements MemoWorksheetTemplateService{
	private final MemoWorksheetTemplateRepo memoWorksheetTemplateRepo;
	private final DealerConfig dealerConfig;

	@Override
	public List<MemoWorksheetTemplate> getMemoWorksheetTemplates(OEM oemId, int year, int version) {
		return memoWorksheetTemplateRepo.findByOemYearAndCountry(oemId.name(), year, version, dealerConfig.getDealerCountryCode());
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
}

