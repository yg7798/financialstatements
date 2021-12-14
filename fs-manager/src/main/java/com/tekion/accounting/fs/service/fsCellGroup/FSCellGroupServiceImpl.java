package com.tekion.accounting.fs.service.fsCellGroup;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class FSCellGroupServiceImpl implements  FSCellGroupService{

	private final OemFsCellGroupRepo cellGroupRepo;
	private final DealerConfig dealerConfig;

	@Override
	public List<AccountingOemFsCellGroup> findGroupCodes(List<String> groupCodes, String oemId, Integer year, Integer version) {
		return cellGroupRepo.findByGroupCodes(oemId, year, version, groupCodes, dealerConfig.getDealerCountryCode());
	}
}
