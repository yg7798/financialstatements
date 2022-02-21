package com.tekion.accounting.fs.service.snapshots;

public interface SnapshotService {
	void createSnapshotsForMappingsAndCellCodes(Integer year, Integer month_1_12);
	void createSnapshotsForMappings(String fsId, Integer month_1_12);
	void createSnapshotsForCellCodes(String fsId, Integer month_1_12);
}
