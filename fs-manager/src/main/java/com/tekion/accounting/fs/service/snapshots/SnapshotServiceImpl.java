package com.tekion.accounting.fs.service.snapshots;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotServiceImpl implements SnapshotService {

	public static boolean DEFAULT_INCLUDE_M13 = true;
	public static boolean DEFAULT_ADD_M13_BALANCES_IN_DEC = false;

	private final FsComputeService fsComputeService;
	private final FSEntryRepo fsEntryRepo;

	@Override
	public void createSnapshotsForMappingsAndCellCodes(Integer year, Integer month_1_12) {
		log.info("called monthClose snapshots creation");
		fsComputeService.createFsMappingAndCellCodeSnapshotForAllSites(year, month_1_12, true, false);
	}

	@Override
	public void createSnapshotsForMappings(String fsId, Integer month_1_12) {
		log.info("called mapping snapshots creation");
		fsComputeService.createFsMappingSnapshot(fsId, month_1_12);
	}

	@Override
	public void createSnapshotsForCellCodes(String fsId, Integer month_1_12) {
		log.info("called cell snapshots creation");
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
		fsComputeService.createFsCellCodeSnapshot(fsEntry, fsEntry.getYear(), month_1_12, DEFAULT_INCLUDE_M13, DEFAULT_ADD_M13_BALANCES_IN_DEC);
	}
}
