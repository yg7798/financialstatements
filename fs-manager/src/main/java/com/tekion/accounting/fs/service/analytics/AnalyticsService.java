package com.tekion.accounting.fs.service.analytics;

import com.tekion.accounting.fs.dto.cellcode.OEMFsCellCodeSnapshotResponseDto;

import java.util.List;
import java.util.Set;

public interface AnalyticsService {
    List<OEMFsCellCodeSnapshotResponseDto> getFSCellCodeAverage(long fromTimestamp, long toTimestamp, Set<String> codes, String oemId);
}
