package com.tekion.accounting.fs.service.pclCodes;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.repos.OemTemplateRepo;
import com.tekion.core.exceptions.TBaseRuntimeException;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PclCodeServiceImplTest extends TestCase {

    @Mock
    OemTemplateRepo oemTemplateRepo;
    @Mock
    OemFsCellGroupRepo oemFsCellGroupRepo;
    @InjectMocks
    PclCodeServiceImpl pclCodeService;

    @Test
    public void testGetOemDetails() {
        Mockito.when(oemTemplateRepo.findAllOemDetails())
                .thenReturn(getOemDetails());
        assertEquals(2, pclCodeService.getOemDetails().size());
    }

    @Test
    public void testGetPclCodeDetails() {
        Mockito.when(oemFsCellGroupRepo.findByOemId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsCellGroupList());
        assertEquals(1, pclCodeService.getPclCodeDetails("GM", 2021, "US").size());
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .findByOemId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testUpdatePclCodeDetails_success() {
        Mockito.when(oemFsCellGroupRepo
                .findByGroupCode(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsCellGroupList().get(0));
        pclCodeService.updatePclCodeDetails(getPclDetailsDto());

        ArgumentCaptor<AccountingOemFsCellGroup> captor = ArgumentCaptor.forClass(AccountingOemFsCellGroup.class);
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1)).save(captor.capture());
        AccountingOemFsCellGroup accountingOemFsCellGroup = captor.getValue();
        assertEquals("_202", accountingOemFsCellGroup.getGroupCode());
        assertEquals("402A", accountingOemFsCellGroup.getAutomatePcl());
        assertEquals("1108A", accountingOemFsCellGroup.getAutosoftPcl());
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .findByGroupCode(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testUpdatePclCodeDetails_Failure() {
        Mockito.when(oemFsCellGroupRepo
                        .findByGroupCode(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(null);
        pclCodeService.updatePclCodeDetails(getPclDetailsDto());
    }


    private List<OemTemplate> getOemDetails() {
        List<OemTemplate> oemDetails = new ArrayList<>();
        OemTemplate oemDetails1 = new OemTemplate();
        oemDetails1.setOemId("GM");
        oemDetails1.setCountry("US");
        oemDetails1.setYear(2021);

        OemTemplate oemDetails2 = new OemTemplate();
        oemDetails2.setOemId("FCA");
        oemDetails2.setCountry("CA");
        oemDetails2.setYear(2021);
        oemDetails.add(oemDetails1);
        oemDetails.add(oemDetails2);
        return oemDetails;
    }

    private List<AccountingOemFsCellGroup> getAccountingOemFsCellGroupList() {
        AccountingOemFsCellGroup accountingOemFsCellGroup = AccountingOemFsCellGroup.builder()
                .oemId("GM")
                .year(2021)
                .groupCode("_202")
                .automatePcl("202A")
                .autosoftPcl("0108A")
                .cdkPcl("1A08")
                .groupDisplayName("202")
                .build();
        return Arrays.asList(accountingOemFsCellGroup);
    }

    private AccountingOemFsCellGroup getPclDetailsDto() {
        AccountingOemFsCellGroup pclDetailsDto = AccountingOemFsCellGroup.builder()
                .oemId("GM")
                .year(2021)
                .groupCode("_202")
                .automatePcl("402A")
                .autosoftPcl("1108A")
                .cdkPcl("1A08")
                .groupDisplayName("202")
                .country("US")
                .build();
        return pclDetailsDto;
    }
}
