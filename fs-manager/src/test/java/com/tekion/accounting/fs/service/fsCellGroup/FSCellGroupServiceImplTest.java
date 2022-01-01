package com.tekion.accounting.fs.service.fsCellGroup;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.event.annotation.AfterTestMethod;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FSCellGroupServiceImplTest extends TestCase {

    @InjectMocks
    FSCellGroupServiceImpl fsCellGroupService;
    @Mock
    DealerConfig dealerConfig;
    @Mock
    OemFsCellGroupRepo cellGroupRepo;

    @Before
    public void setUp() {
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
    }

    @AfterTestMethod
    public void cleanUp() {
        Mockito.reset(cellGroupRepo);
    }

    @Test
    public void testFindGroupCodes() {
        Mockito.when(cellGroupRepo.findByGroupCodes(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyList(), Mockito.anyString())).thenReturn(getAccountingOemFsCellGroupList());
        assertEquals(getAccountingOemFsCellGroupList(), fsCellGroupService.findGroupCodes(new ArrayList<>(), "Acura", 2022, 1));
    }

    private List<AccountingOemFsCellGroup> getAccountingOemFsCellGroupList() {
        List<AccountingOemFsCellGroup> list = new ArrayList<>();
        AccountingOemFsCellGroup accountingOemFsCellGroup = new AccountingOemFsCellGroup();
        accountingOemFsCellGroup.setOemId("Acura");
        accountingOemFsCellGroup.setCountry("US");
        accountingOemFsCellGroup.setGroupCode("123");
        accountingOemFsCellGroup.setVersion(1);
        accountingOemFsCellGroup.setYear(2022);
        list.add(accountingOemFsCellGroup);
        return list;
    }
}
