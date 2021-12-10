package com.tekion.accounting.fs.service.utils;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.accountingInfo.FSPreferences;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.OEMFsCellCodeSnapshot;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.dto.context.FsReportContext;
import com.tekion.accounting.fs.service.compute.models.OemFsCellContext;
import com.tekion.core.exceptions.TBaseRuntimeException;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FinancialStatementUtilsTest extends TestCase {


    @Test
    public void testValidOemId(){
        FinancialStatementUtils.validateOemId("GM");
    }

    @Test(
        expected = TBaseRuntimeException.class
    )
    public void testInvalidOemId(){
        FinancialStatementUtils.validateOemId("Dummy");
    }

    @Test
    public void testGetSnapshotValue() {
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("month", "1");
        AccountingOemFsCellCode accountingOemFsCellCode = AccountingOemFsCellCode.builder()
                .code("_11")
                .additionalInfo(additionalInfo)
        .build();
        assertEquals(new BigDecimal(10), FinancialStatementUtils.getSnapshotValue(accountingOemFsCellCode, getOemFsCellContext()));
    }

    @Test
    public void testGetSnapshotValue_whenMonthInfoIsAbsent() {
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("month", "5");
        AccountingOemFsCellCode accountingOemFsCellCode = AccountingOemFsCellCode.builder()
                .code("_11")
                .additionalInfo(additionalInfo)
                .build();
        assertNull(FinancialStatementUtils.getSnapshotValue(accountingOemFsCellCode, getOemFsCellContext()));
    }

    @Test
    public void testUseSnapshotValuesInFS(){
        OemFsCellContext context = getOemFsCellContext();
        assertEquals(true, FinancialStatementUtils.useSnapshotValuesInFS(context));

        context.setAccountingInfo(null);
        assertFalse(FinancialStatementUtils.useSnapshotValuesInFS(context));

        FSPreferences fsPreferences = new FSPreferences();
        fsPreferences.setUseSnapshotsForMonthlyCells(true);
        OemConfig oemConfig = OemConfig.builder()
                .fsPreferences(fsPreferences)
                .build();
        context.setOemConfig(oemConfig);
        assertEquals(true, FinancialStatementUtils.useSnapshotValuesInFS(context));
    }

    @Test
    public void testIsRoundOffGLBalances() {
        FsReportContext context = getFsReportContextWithAccInfo();
        assertTrue(FinancialStatementUtils.isRoundOffGLBalances(context));

        context.setAccountingInfo(null);
        assertFalse(FinancialStatementUtils.isRoundOffGLBalances(context));

        assertFalse(FinancialStatementUtils.isRoundOffGLBalances(getFsReportContextWithOemConfig()));
    }

    private FsReportContext getFsReportContextWithAccInfo() {

        FSPreferences fsPreferences = new FSPreferences();
        fsPreferences.setUseSnapshotsForMonthlyCells(true);
        Map<String, Boolean> map = new HashMap<>();
        map.put("GM", true);
        map.put("FCA", false);
        fsPreferences.setRoundOffGlAccountBalances(map);

        AccountingInfo accountingInfo = new AccountingInfo();
        accountingInfo.setFsPreferences(fsPreferences);

        FsReportContext context = FsReportContext.builder()
                .oemId("GM")
                .accountingInfo(accountingInfo)
                .build();
        return context;
    }

    private FsReportContext getFsReportContextWithOemConfig() {

        FSPreferences fsPreferences = new FSPreferences();
        fsPreferences.setUseSnapshotsForMonthlyCells(true);
        Map<String, Boolean> map = new HashMap<>();
        map.put("GM", true);
        map.put("FCA", false);
        fsPreferences.setRoundOffGlAccountBalances(map);

        OemConfig oemConfig = new OemConfig();
        oemConfig.setFsPreferences(fsPreferences);

        FsReportContext context = FsReportContext.builder()
                .oemId("FCA")
                .oemConfig(oemConfig)
                .build();
        return context;
    }

    private OemFsCellContext getOemFsCellContext() {

        FSPreferences fsPreferences = new FSPreferences();
        fsPreferences.setUseSnapshotsForMonthlyCells(true);

        AccountingInfo accountingInfo = new AccountingInfo();
        accountingInfo.setFsPreferences(fsPreferences);

        Map<Integer, Map<String, OEMFsCellCodeSnapshot>> monthToCellCodeSnapshots = new HashMap<>();
        monthToCellCodeSnapshots.put(1, new HashMap<>());
        OEMFsCellCodeSnapshot oemFsCellCodeSnapshot = new OEMFsCellCodeSnapshot();
        oemFsCellCodeSnapshot.setCode("_11");
        oemFsCellCodeSnapshot.setMonth(1);
        oemFsCellCodeSnapshot.setValue(new BigDecimal(10));
        monthToCellCodeSnapshots.get(1).put("_11", oemFsCellCodeSnapshot);

        monthToCellCodeSnapshots.put(2, new HashMap<>());
        oemFsCellCodeSnapshot = new OEMFsCellCodeSnapshot();
        oemFsCellCodeSnapshot.setCode("_15");
        oemFsCellCodeSnapshot.setMonth(2);
        oemFsCellCodeSnapshot.setValue(new BigDecimal(50));
        monthToCellCodeSnapshots.get(1).put("_15", oemFsCellCodeSnapshot);
       OemFsCellContext oemFsCellContext =
               OemFsCellContext.builder()
                       .monthToCellCodeSnapshots(monthToCellCodeSnapshots)
                       .accountingInfo(accountingInfo)
                       .build();
       return oemFsCellContext;
    }


}
