package com.tekion.accounting.service.accountingInfo;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.dto.accountingInfo.AccountingInfoDto;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.accountingInfo.AccountingInfoRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoServiceImpl;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.*;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.Silent.class)
@PrepareForTest(DpUtils.class)
public class AccountingInfoServiceImplTest extends TestCase {

    @Mock
    DealerConfig dealerConfig;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    AccountingInfoRepo infoRepo;
    @InjectMocks
    AccountingInfoServiceImpl accountingInfoService;


    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getTimeZone("America/Los_Angeles"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
    }

    @Test
    public void testSaveOrUpdate(){
        AccountingInfoDto dto = new AccountingInfoDto();
        dto.setPrimaryOEM("GM");
        AccountingInfo currentInfo = dto.toAccountingInfo();
        AccountingInfo accountingInfo = getAccountingInfo();
        Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyString())).thenReturn(accountingInfo);
        Mockito.when(infoRepo.save(currentInfo)).thenReturn(currentInfo);
        AccountingInfo aInfo = accountingInfoService.saveOrUpdate(dto);
        assertEquals(currentInfo, aInfo);
    }

    @Test
    public void testFind(){
        Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyString())).thenReturn(getAccountingInfo());
        AccountingInfo info = accountingInfoService.find("5");
        assertNotNull(info);
    }

   @Test
   public void testFindList(){
       Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyList())).thenReturn(getAccountingInfoList());
       List<AccountingInfo> accountingInfos = accountingInfoService.findList(new ArrayList<String>(Collections.singleton("5")));
       assertEquals(2, accountingInfos.size());
    }

    @Test
    public void testDelete(){
        AccountingInfo accountingInfo = getAccountingInfo();
        Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyString())).thenReturn(accountingInfo);
        Mockito.when(infoRepo.save(accountingInfo)).thenReturn(accountingInfo);
        AccountingInfo info = accountingInfoService.delete("5");
        assertTrue(info.isDeleted());
    }

    @Test
    public void testPopulateOEMFields(){
        AccountingInfo accountingInfo = getAccountingInfo();
        Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyString())).thenReturn(accountingInfo);
        Mockito.when(fsEntryRepo.fetchAllByDealerIdNonDeleted(Mockito.anyString())).thenReturn(getFsEntries());
        Mockito.when(infoRepo.save(accountingInfo)).thenReturn(accountingInfo);
        AccountingInfo info = accountingInfoService.populateOEMFields();
        assertEquals(getFsEntries().stream().map(FSEntry::getOemId).collect(Collectors.toSet()), info.getSupportedOEMs());
    }


    @Test
    public void testAddOem(){
        AccountingInfo accountingInfo = getAccountingInfo();
        Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyString())).thenReturn(accountingInfo);
        Mockito.when(infoRepo.save(accountingInfo)).thenReturn(accountingInfo);
        AccountingInfo aInfo = accountingInfoService.addOem(OEM.GM);
        assertNotNull(aInfo);
    }

    @Test
    public void testRemoveOem(){
        AccountingInfo accountingInfo = getAccountingInfo();
        Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyString())).thenReturn(getAccountingInfo());
        Mockito.when(infoRepo.save(accountingInfo)).thenReturn(accountingInfo);
        AccountingInfo aInfo = accountingInfoService.removeOem(OEM.GM);
        assertNotNull(aInfo);
    }

    @Test
    public void testSetPrimaryOem(){
        AccountingInfo accountingInfo = getAccountingInfo();
        Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyString())).thenReturn(accountingInfo);
        Mockito.when(infoRepo.save(accountingInfo)).thenReturn(accountingInfo);
        AccountingInfo aInfo = accountingInfoService.setPrimaryOem(OEM.GM);
        assertEquals(OEM.GM.name(), aInfo.getPrimaryOEM());
    }

    @Test
    public void testMigrateFsRoundOffOffset(){
        AccountingInfo accountingInfo = getAccountingInfo();
        Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyString())).thenReturn(accountingInfo);
        Mockito.when(infoRepo.save(accountingInfo)).thenReturn(accountingInfo);
        accountingInfoService.migrateFsRoundOffOffset();
    }



    private AccountingInfo getAccountingInfo() {
        AccountingInfo accountingInfo = new AccountingInfo();
        accountingInfo.setId("1");
        accountingInfo.setPrimaryOEM(OEM.GM.getOem());
        accountingInfo.setSupportedOEMs(new HashSet<String>(){{
            add("Ford");
            add("FCA");
        }});
        accountingInfo.setBsdPresent(true);
        accountingInfo.setDealerId("5");
        return accountingInfo;
    }

    private List<AccountingInfo> getAccountingInfoList() {
        List<AccountingInfo> list = new ArrayList<>();
        AccountingInfo accountingInfo = new AccountingInfo();
        accountingInfo.setPrimaryOEM(OEM.GM.getOem());
        accountingInfo.setSupportedOEMs(new HashSet<String>());
        accountingInfo.setBsdPresent(true);
        accountingInfo.setDealerId("5");

        AccountingInfo accountingInfo2 = new AccountingInfo();
        accountingInfo2.setPrimaryOEM(OEM.FCA.getOem());
        accountingInfo2.setSupportedOEMs(new HashSet<String>());
        accountingInfo2.setBsdPresent(false);
        accountingInfo2.setDealerId("5");

        list.add(accountingInfo);
        list.add(accountingInfo2);
        return list;
    }

    private List<FSEntry> getFsEntries(){
        FSEntry fsEntry= new FSEntry();
        fsEntry.setOemId("GM");
        fsEntry.setFsType(FSType.OEM.name());
        fsEntry.setYear(2021);
        fsEntry.setDealerId("5");
        return new ArrayList<>(Collections.singletonList(fsEntry));
    }

}