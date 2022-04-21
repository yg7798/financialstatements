package com.tekion.accounting.fs.service.fsCellGroup;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.dto.cellGrouop.ValidateGroupCodeResponseDto;
import com.tekion.accounting.fs.repos.FSCellCodeRepo;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.core.exceptions.TBaseRuntimeException;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.event.annotation.AfterTestMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FSCellGroupServiceImplTest extends TestCase {

    @InjectMocks
    FSCellGroupServiceImpl fsCellGroupService;
    @Mock
    DealerConfig dealerConfig;
    @Mock
    OemFsCellGroupRepo cellGroupRepo;
    @Mock
    FsComputeService computeService;
    @Captor
    private ArgumentCaptor<ArrayList<AccountingOemFsCellGroup>> captor;

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

    @Test
    public void testMigrateCellGroupValuesForOem() {
        Mockito.when(cellGroupRepo.findByOemIds(Mockito.anySet(), Mockito.anySet(), Mockito.anyString())).thenReturn(getAccountingOemFsCellGroupList());
        fsCellGroupService.migrateCellGroupValuesForOem("US", 2021, 2022, getOemIds());
        Mockito.verify(cellGroupRepo, Mockito.times(1)).upsertBulk(captor.capture());
        List<AccountingOemFsCellGroup> capturedArgument = captor.getValue();
        assertEquals(capturedArgument.get(0).getGroupCode(), getExistCellGroupsFromYear().get(0).getGroupCode());
        assertEquals(capturedArgument.get(0).getAutomatePcl(), getExistCellGroupsFromYear().get(0).getAutomatePcl());
        assertEquals(capturedArgument.get(0).getAutosoftPcl(), getExistCellGroupsFromYear().get(0).getAutosoftPcl());
        assertEquals(capturedArgument.get(0).getCdkPcl(), getExistCellGroupsFromYear().get(0).getCdkPcl());
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testMigrateCellGroupValuesForOemFromYearEmpty() {
        Mockito.when(cellGroupRepo.findByOemIds(Mockito.anySet(), Mockito.anySet(), Mockito.anyString())).thenReturn(getAccountingOemFsCellGroupToYear());
        fsCellGroupService.migrateCellGroupValuesForOem("US", 2021, 2022, getOemIds());
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testMigrateCellGroupValuesForOemToYearEmpty() {
        Mockito.when(cellGroupRepo.findByOemIds(Mockito.anySet(), Mockito.anySet(), Mockito.anyString())).thenReturn(getExistCellGroupsFromYear());
        fsCellGroupService.migrateCellGroupValuesForOem("US", 2021, 2022, getOemIds());
    }

    @Test
    public void testFindInValidAndMissingGroupCodes() {
        Mockito.when(computeService.getOemTMappingList(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).thenReturn(getCellCodes());
        Mockito.when(cellGroupRepo.findNonDeletedByOemIdYearVersionAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).thenReturn(getCellGroups());
        assertEquals(getValidateGroupCodeResponseDto(), fsCellGroupService.findInvalidAndMissingGroupCodes("Acura", 2022, "US"));
    }

    private List<AccountingOemFsCellGroup> getCellGroups() {
        List<AccountingOemFsCellGroup> cellGroups = getAccountingOemFsCellGroupToYear();
        AccountingOemFsCellGroup accountingOemFsCellGroup = new AccountingOemFsCellGroup();
        accountingOemFsCellGroup.setOemId("Acura");
        accountingOemFsCellGroup.setCountry("US");
        accountingOemFsCellGroup.setGroupCode("456");
        accountingOemFsCellGroup.setVersion(1);
        accountingOemFsCellGroup.setYear(2022);
        cellGroups.add(accountingOemFsCellGroup);
        return cellGroups;
    }

    private List<AccountingOemFsCellCode> getCellCodes() {
        AccountingOemFsCellCode cellCode = new AccountingOemFsCellCode();
        cellCode.setOemId("Acura");
        cellCode.setCountry("US");
        cellCode.setVersion(1);
        cellCode.setYear(2022);
        cellCode.setGroupCode("123");

        AccountingOemFsCellCode cellCode1 = new AccountingOemFsCellCode();
        cellCode1.setOemId("Acura");
        cellCode1.setCountry("US");
        cellCode1.setVersion(1);
        cellCode1.setYear(2022);
        cellCode1.setGroupCode("1000");

        List<AccountingOemFsCellCode> cellCodes = new ArrayList<>();
        cellCodes.add(cellCode);
        cellCodes.add(cellCode1);
        return cellCodes;
    }

    private ValidateGroupCodeResponseDto getValidateGroupCodeResponseDto() {
        ValidateGroupCodeResponseDto dto = new ValidateGroupCodeResponseDto();

        List<String> groupCodesToRemove = new ArrayList<>();
        groupCodesToRemove.add("456");

        List<String> groupCodesToAdd = new ArrayList<>();
        groupCodesToAdd.add("1000");

        dto.setGroupCodesToRemove(groupCodesToRemove);
        dto.setGroupCodesToAdd(groupCodesToAdd);
        return dto;
    }

    private Set<String> getOemIds() {
        Set<String> oemIds = new HashSet<>();
        oemIds.add("Acura");
        return oemIds;
    }

    private List<AccountingOemFsCellGroup> getAccountingOemFsCellGroupToYear() {
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

    private List<AccountingOemFsCellGroup> getExistCellGroupsFromYear() {
        List<AccountingOemFsCellGroup> list = new ArrayList<>();
        AccountingOemFsCellGroup accountingOemFsCellGroup = new AccountingOemFsCellGroup();
        accountingOemFsCellGroup.setOemId("Acura");
        accountingOemFsCellGroup.setCountry("US");
        accountingOemFsCellGroup.setGroupCode("123");
        accountingOemFsCellGroup.setVersion(1);
        accountingOemFsCellGroup.setYear(2021);
        accountingOemFsCellGroup.setAutomatePcl("123");
        accountingOemFsCellGroup.setAutosoftPcl("456");
        accountingOemFsCellGroup.setCdkPcl("xyz");
        list.add(accountingOemFsCellGroup);
        return list;
    }

    private List<AccountingOemFsCellGroup> getAccountingOemFsCellGroupList() {
        List<AccountingOemFsCellGroup> list = new ArrayList<>();
        AccountingOemFsCellGroup accountingOemFsCellGroup = new AccountingOemFsCellGroup();
        accountingOemFsCellGroup.setOemId("Acura");
        accountingOemFsCellGroup.setCountry("US");
        accountingOemFsCellGroup.setGroupCode("123");
        accountingOemFsCellGroup.setVersion(1);
        accountingOemFsCellGroup.setYear(2022);

        AccountingOemFsCellGroup accountingOemFsCellGroup1 = new AccountingOemFsCellGroup();
        accountingOemFsCellGroup1.setOemId("Acura");
        accountingOemFsCellGroup1.setCountry("US");
        accountingOemFsCellGroup1.setGroupCode("123");
        accountingOemFsCellGroup1.setVersion(1);
        accountingOemFsCellGroup1.setYear(2021);
        accountingOemFsCellGroup1.setAutomatePcl("123");
        accountingOemFsCellGroup1.setAutosoftPcl("456");
        accountingOemFsCellGroup1.setCdkPcl("xyz");

        list.add(accountingOemFsCellGroup1);
        list.add(accountingOemFsCellGroup);
        return list;
    }
}
