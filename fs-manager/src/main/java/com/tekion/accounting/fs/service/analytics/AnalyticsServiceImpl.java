package com.tekion.accounting.fs.service.analytics;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.OEMFsCellCodeSnapshot;
import com.tekion.accounting.fs.dto.cellcode.OEMFsCellCodeSnapshotResponseDto;
import com.tekion.accounting.fs.repos.OEMFsCellCodeSnapshotRepo;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService{

    private final OEMFsCellCodeSnapshotRepo cellCodeSnapshotRepo;
    @Override
    public List<OEMFsCellCodeSnapshotResponseDto> getFSCellCodeAverage(long fromTimestamp, long toTimestamp, Set<String> codes,
                                                                       String oemId, String siteId) {
        Map<String, List<OEMFsCellCodeSnapshot>> map = Maps.newHashMap();
        List<OEMFsCellCodeSnapshotResponseDto> oemFsCellCodeSnapshotResponseDtoList = Lists.newArrayList();
        List<OEMFsCellCodeSnapshot> oemFsCellCodeSnapshotList = cellCodeSnapshotRepo.getFsCellCodeByTimestamp(fromTimestamp, toTimestamp, codes,
                oemId, UserContextProvider.getCurrentDealerId(), siteId);

        oemFsCellCodeSnapshotList.forEach(cell -> {
            if (!map.containsKey(cell.getCode())) {
                map.put(cell.getCode(), Lists.newArrayList());
            }
            map.get(cell.getCode()).add(cell);
        });

        map.keySet().forEach(key -> {
            BigDecimal avg = new BigDecimal(0);
            for (OEMFsCellCodeSnapshot cell : map.get(key)) {
                avg = avg.add(cell.getValue());
            }
            avg = avg.divide(BigDecimal.valueOf(map.get(key).size()),2, RoundingMode.HALF_UP);
            OEMFsCellCodeSnapshotResponseDto oemFsCellCodeSnapshotResponseDto = new OEMFsCellCodeSnapshotResponseDto(
                    key,
                    avg
            );
            oemFsCellCodeSnapshotResponseDtoList.add(oemFsCellCodeSnapshotResponseDto);
        });

        return oemFsCellCodeSnapshotResponseDtoList;
    }
}
