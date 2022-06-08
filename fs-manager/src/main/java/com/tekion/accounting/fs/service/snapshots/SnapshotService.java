package com.tekion.accounting.fs.service.snapshots;

import com.tekion.accounting.events.UpdateSnapshotsEvent;
import com.tekion.accounting.fs.beans.mappings.OemFsMappingSnapshot;

import java.util.List;

public interface SnapshotService {
	void createSnapshotsForMappingsAndCellCodes(Integer year, Integer month_1_12);
	void createSnapshotsForMappings(String fsId, Integer month_1_12);
	void createSnapshotsForCellCodes(String fsId, Integer month_1_12);
	List<OemFsMappingSnapshot> getMappingSnapshots(String fsId, Integer month_1_12);

	void updateSnapshots(UpdateSnapshotsEvent event);
}
