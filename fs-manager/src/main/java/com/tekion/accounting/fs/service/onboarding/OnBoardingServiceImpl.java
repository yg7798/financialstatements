package com.tekion.accounting.fs.service.onboarding;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OnBoardingServiceImpl implements OnBoardingService {

    public static boolean DEFAULT_INCLUDE_M13 = true;
    public static boolean DEFAULT_ADD_M13_BALANCES_IN_DEC = false;

    private final FsComputeService fsComputeService;
    private final FSEntryRepo fsEntryRepo;
    private final AccountingInfoService accountingInfoService;
    private final AccountingService accountingService;

    @Override
    public void createSnapshotsToOnboardNewDealer() {
        accountingInfoService.populateOEMFields();
        List<FSEntry> fsEntries = fsEntryRepo.getAllFSEntriesByFsType(FSType.OEM.name(), UserContextProvider.getCurrentDealerId());
        MonthInfo monthInfo = accountingService.getActiveMonthInfo();
        fsEntries.stream().forEach(fsEntry -> {
            createSnapshotForMonthRange(fsEntry, monthInfo, monthInfo.getYear() - 1, monthInfo.getYear()-1,
                    Calendar.JANUARY + 1, Calendar.DECEMBER + 1, DEFAULT_INCLUDE_M13, DEFAULT_ADD_M13_BALANCES_IN_DEC);
            createSnapshotForMonthRange(fsEntry, monthInfo, monthInfo.getYear(), monthInfo.getYear(),
                    Calendar.JANUARY + 1, monthInfo.getMonth(), DEFAULT_INCLUDE_M13, DEFAULT_ADD_M13_BALANCES_IN_DEC);
        });
    }

    private void createSnapshotForMonthRange(FSEntry fsEntry, MonthInfo monthInfo, int oemFsYear, int year, int fromMonth_1_12, int toMonth_1_12, boolean includeM13, boolean addM13BalInDecBalances) {
        if (monthInfo.getYear() == year && monthInfo.getMonth() + 1 <= toMonth_1_12) {
            throw new TBaseRuntimeException("Year and toMonth_1_12 are invalid because snapshot cannot be create for current month");
        }
        while (year <= monthInfo.getYear() && fromMonth_1_12 <= toMonth_1_12) {
            fsComputeService.createFsCellCodeSnapshot(fsEntry, oemFsYear, fromMonth_1_12, includeM13, addM13BalInDecBalances);
            log.info("Successfully created snapshot for Year {} & Month {}", year, fromMonth_1_12);
            fromMonth_1_12++;
        }
    }
}
