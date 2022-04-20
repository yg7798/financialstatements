package com.tekion.accounting.fs.service.analytics;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.OEMFsCellCodeSnapshot;
import com.tekion.accounting.fs.common.date.utils.DateFilter;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.common.utils.MonthUtils;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.cellcode.OEMFsCellCodeSnapshotResponseDto;
import com.tekion.accounting.fs.repos.OEMFsCellCodeSnapshotRepo;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.as.models.beans.GLPostingES;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.es.request.ESResponse;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static java.util.Calendar.JANUARY;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService{

    private final AccountingService accountingService;
    private final OEMFsCellCodeSnapshotRepo cellCodeSnapshotRepo;
    @Override
    public List<OEMFsCellCodeSnapshotResponseDto> getFSCellCodeAverage(long fromTimestamp, long toTimestamp, Set<String> codes,
                                                                       String oemId) {
        Map<String, List<OEMFsCellCodeSnapshot>> map = Maps.newHashMap();
        List<OEMFsCellCodeSnapshotResponseDto> oemFsCellCodeSnapshotResponseDtoList = Lists.newArrayList();
        List<OEMFsCellCodeSnapshot> oemFsCellCodeSnapshotList = cellCodeSnapshotRepo.getFsCellCodeByTimestamp(fromTimestamp, toTimestamp, codes,
                oemId, UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());

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
