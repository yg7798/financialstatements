package com.tekion.accounting.fs.service.onboarding;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OEMFsCellCodeSnapshotRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.beans.TBaseMongoBean;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.common.AsyncContextDecorator.ASYNC_THREAD_POOL;

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
    private final OEMFsCellCodeSnapshotRepo oemFsCellCodeSnapshotRepo;

    @Qualifier(ASYNC_THREAD_POOL)
    @Autowired
    private AsyncTaskExecutor executorService;

    @Override
    public void createSnapshotsToOnboardNewDealer() {
        accountingInfoService.populateOEMFields();
        MonthInfo monthInfo = accountingService.getActiveMonthInfo();

        List<Integer> years = new ArrayList<>();
        years.add(monthInfo.getYear() - 1);
        years.add(monthInfo.getYear());
        List<FSEntry> fsEntries = TCollectionUtils.nullSafeList(fsEntryRepo.findFsEntriesForDealerAndYears(FSType.OEM, years, UserContextProvider.getCurrentDealerId()));
        Runnable runAsync = () -> {
            createSnapshotsToOnboardNewDealer(fsEntries, monthInfo);
        };
        executorService.execute(runAsync);
    }

    private void createSnapshotsToOnboardNewDealer(List<FSEntry> fsEntries, MonthInfo monthInfo) {
        oemFsCellCodeSnapshotRepo.hardDeleteSnapshotByFsIdAndMonth(fsEntries.stream().map(TBaseMongoBean::getId).collect(Collectors.toSet()), UserContextProvider.getCurrentDealerId());
        fsEntries.forEach(fsEntry -> {
            if (monthInfo.getYear().equals(fsEntry.getYear())) {
                try {
                    createSnapshotForMonthRange(fsEntry, monthInfo, fsEntry.getYear(), fsEntry.getYear(),
                            Calendar.JANUARY + 1, monthInfo.getMonth(), DEFAULT_INCLUDE_M13, DEFAULT_ADD_M13_BALANCES_IN_DEC);
                } catch (Exception e) {
                    log.error(String.format("error while creating snapshot for fsId %s", fsEntry.getOemId()), e);
                }

            } else {

                try {
                    createSnapshotForMonthRange(fsEntry, monthInfo, fsEntry.getYear(), fsEntry.getYear(),
                            Calendar.JANUARY + 1, Calendar.DECEMBER + 1, DEFAULT_INCLUDE_M13, DEFAULT_ADD_M13_BALANCES_IN_DEC);
                } catch (Exception e) {
                    log.error(String.format("error while creating snapshot for fsId %s", fsEntry.getOemId()), e);
                }
            }
        });

    }

    private void createSnapshotForMonthRange(FSEntry fsEntry, MonthInfo monthInfo, int oemFsYear, int year, int fromMonth_1_12, int toMonth_1_12, boolean includeM13, boolean addM13BalInDecBalances) {
        while (year <= monthInfo.getYear() && fromMonth_1_12 <= toMonth_1_12) {
            fsComputeService.createFsCellCodeSnapshot(fsEntry, oemFsYear, fromMonth_1_12, includeM13, addM13BalInDecBalances);
            log.info("Successfully created snapshot for Year {} & Month {}", year, fromMonth_1_12);
            fromMonth_1_12++;
        }
    }
}
