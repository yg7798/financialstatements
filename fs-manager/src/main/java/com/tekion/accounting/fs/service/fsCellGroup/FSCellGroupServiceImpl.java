package com.tekion.accounting.fs.service.fsCellGroup;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class FSCellGroupServiceImpl implements  FSCellGroupService{

	private final OemFsCellGroupRepo cellGroupRepo;
	private final DealerConfig dealerConfig;

	@Override
	public List<AccountingOemFsCellGroup> findGroupCodes(List<String> groupCodes, String oemId, Integer year, Integer version) {
		return cellGroupRepo.findByGroupCodes(oemId, year, version, groupCodes, dealerConfig.getDealerCountryCode());
	}

	@Override
	public void migrateCellGroupValuesForOem(String country, Integer fromYear, Integer toYear, Set<String> oemIds) {
		Set<Integer> years = new HashSet<>();
		years.add(fromYear);
		years.add(toYear);

		List<AccountingOemFsCellGroup> cellGroups = cellGroupRepo.findByOemIds(oemIds, years, country);
		Map<String, List<AccountingOemFsCellGroup>> oemIdVsCellGroups = getOemIdVsCellGroupsMap(cellGroups);

		Map<String, AccountingOemFsCellGroup> fromGroupCodeVsCellGroups = new HashMap<>();
		List<AccountingOemFsCellGroup> cellGroupValuesToUpsert = new ArrayList<>();
		for (String oemId : oemIds) {
			List<AccountingOemFsCellGroup> fsCellGroups = oemIdVsCellGroups.get(oemId);
			List<AccountingOemFsCellGroup> cellGroupsToYear = new ArrayList<>();

			for (AccountingOemFsCellGroup cellGroup : TCollectionUtils.nullSafeList(fsCellGroups)) {
				if (fromYear.equals(cellGroup.getYear())) {
					fromGroupCodeVsCellGroups.put(cellGroup.getGroupCode(), cellGroup);
				} else {
					cellGroupsToYear.add(cellGroup);
				}
			}
			if (fromGroupCodeVsCellGroups.isEmpty()) {
				throw new TBaseRuntimeException(FSError.cellGroupsFoundEmpty, String.valueOf(fromYear), oemId);
			}

			if (TCollectionUtils.isEmpty(cellGroupsToYear)) {
				throw new TBaseRuntimeException(FSError.cellGroupsFoundEmpty, String.valueOf(toYear), oemId);
			}

			for (AccountingOemFsCellGroup toCellGroup : TCollectionUtils.nullSafeList(cellGroupsToYear)) {
				if (fromGroupCodeVsCellGroups.containsKey(toCellGroup.getGroupCode())) {
					toCellGroup.updatePclCodes(fromGroupCodeVsCellGroups.get(toCellGroup.getGroupCode()));
					cellGroupValuesToUpsert.add(toCellGroup);
				}
			}
			fromGroupCodeVsCellGroups.clear();
		}

		cellGroupRepo.upsertBulk(cellGroupValuesToUpsert);
	}

	private Map<String, List<AccountingOemFsCellGroup>> getOemIdVsCellGroupsMap(List<AccountingOemFsCellGroup> cellGroups) {
		Map<String, List<AccountingOemFsCellGroup>> oemIdVsCellGroups = new HashMap<>();

		for (AccountingOemFsCellGroup cellGroup : cellGroups) {
			if (oemIdVsCellGroups.containsKey(cellGroup.getOemId())) {
				List<AccountingOemFsCellGroup> fsCellGroups = oemIdVsCellGroups.get(cellGroup.getOemId());
				fsCellGroups.add(cellGroup);
			} else {
				List<AccountingOemFsCellGroup> fsCellGroups = new ArrayList<>();
				fsCellGroups.add(cellGroup);
				oemIdVsCellGroups.put(cellGroup.getOemId(), fsCellGroups);
			}
		}
		return oemIdVsCellGroups;
	}
}
