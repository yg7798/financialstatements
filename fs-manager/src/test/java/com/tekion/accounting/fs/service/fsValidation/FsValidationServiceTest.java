package com.tekion.accounting.fs.service.fsValidation;

import com.google.common.collect.Maps;
import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.fsValidation.FsValidationRule;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.cellcode.FsCodeDetail;
import com.tekion.accounting.fs.dto.fsValidation.FsValidationResult;
import com.tekion.accounting.fs.dto.fsValidation.FsValidationRuleDto;
import com.tekion.accounting.fs.repos.*;
import com.tekion.accounting.fs.repos.fsValidation.FsValidationRepoImpl;
import com.tekion.accounting.fs.service.common.FileCommons;
import com.tekion.accounting.fs.service.common.parsing.ScriptParser;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.accouting.fs.factory.ExcelFactory;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UserContextUtils.class)
public class FsValidationServiceTest extends TestCase {

	@InjectMocks
	FsValidationServiceImpl validationService;

	@Mock
	DealerConfig dealerConfig;
	@Mock
	FSEntryRepo fsEntryRepo;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	FsValidationRepoImpl fsValidationRepo;
	@Mock
	FsComputeService computeService;
	@Mock
	FileCommons fileCommons;
	@Mock
	ScriptParser scriptParser;

	@Before
	public void setUp() throws ExecutionException {
		UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
		Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getTimeZone("America/Los_Angeles"));
		Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
	}

	@Test
	public void testImport() throws IOException, ScriptException {
		File f = getRulesFile();
		Mockito.when(fileCommons.downloadFileUsingMediaId(anyString())).thenReturn(f);
		Mockito.when(scriptParser.eval(anyString())).thenReturn(Boolean.TRUE);
		validationService.importRules("", "GM", "US", 2022);
		ArgumentCaptor<List<FsValidationRule>> argumentCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(fsValidationRepo).bulkUpsert(argumentCaptor.capture());
		List<FsValidationRule> rules = argumentCaptor.getValue();
		Assert.assertEquals(1, rules.size());
		Assert.assertEquals("US", rules.get(0).getCountry());
		f.delete();
	}

	@Test
	public void testSave(){
		List<FsValidationRuleDto> dtos =  getFsValidationDtos();
		validationService.save(dtos);
		ArgumentCaptor<List<FsValidationRule>> argumentCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(fsValidationRepo).bulkUpsert(argumentCaptor.capture());
		List<FsValidationRule> rules = argumentCaptor.getValue();
		Assert.assertEquals(2, rules.size());
		Assert.assertEquals("US", rules.get(1).getCountry());
	}

	@Test
	public void testCopyRules(){
		Integer year_2023 = 2023;
		List<FsValidationRuleDto> dtos =  getFsValidationDtos();
		Mockito.when(fsValidationRepo.getValidationRules(anyString(), anyCollection(), anyString())).thenReturn(getFsValidationRules());
		validationService.copyRules("GM", "US", 2022, year_2023);
		ArgumentCaptor<List<FsValidationRule>> argumentCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(fsValidationRepo).bulkUpsert(argumentCaptor.capture());
		List<FsValidationRule> rules = argumentCaptor.getValue();
		Assert.assertEquals(2, rules.size());
		Assert.assertEquals(year_2023, rules.get(1).getYear());
	}

	@Test
	public void testValidateFs() throws ScriptException {
		Mockito.when(scriptParser.eval(anyString())).thenReturn(Boolean.FALSE);
		List<FsValidationRule> rules =  getFsValidationRules();
		Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString())).
				thenReturn(FSEntry.builder().fsType("OEM").year(2022).oemId("GM").build());
		Mockito.when(fsValidationRepo.getValidationRules(anyString(), anyList(), anyString())).thenReturn(rules);
		Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
		Mockito.when(computeService.computeFsCellCodeDetails(any(FSEntry.class),
				anyLong(), anyBoolean(), anyBoolean())).thenReturn(getCellCodeDetails());
		List<FsValidationResult> response = validationService.validateFs("", 1l, false, true);
		Assert.assertEquals(2, response.size());
		Assert.assertEquals("code1", response.get(0).getCellCode());
	}

	List<FsValidationRuleDto> getFsValidationDtos(){
		List<FsValidationRuleDto> dtos = new ArrayList<>();
		FsValidationRuleDto dto1 = new FsValidationRuleDto();
		dto1.setCountry("US");
		dto1.setOemId("GM");
		dto1.setYear(2022);
		dto1.setOemCode("100");
		dto1.setErrorCode("E1");
		dto1.setType("WARNING");
		dto1.setExpression("(${100}) > 0");
		dtos.add(dto1);

		FsValidationRuleDto dto2 = new FsValidationRuleDto();
		dto2.setOemCode("100");
		dto2.setErrorCode("E1");
		dto2.setType("WARNING");
		dto2.setExpression("(${200}) > 0");
		dtos.add(dto2);

		return dtos;
	}

	List<FsValidationRule> getFsValidationRules(){
		List<FsValidationRule> dtos = new ArrayList<>();
		FsValidationRule dto1 = new FsValidationRule();
		dto1.setId("id1");
		dto1.setCountry("US");
		dto1.setOemId("GM");
		dto1.setYear(2022);
		dto1.setOemCode("100");
		dto1.setErrorCode("E1");
		dto1.setType("WARNING");
		dto1.setExpression("(${100}) > 0");
		dtos.add(dto1);

		FsValidationRule dto2 = new FsValidationRule();
		dto2.setId("id2");
		dto2.setYear(2022);
		dto1.setCountry("US");
		dto2.setErrorCode("E2");
		dto2.setType("WARNING");
		dto2.setOemCode("200");
		dto2.setExpression("(${200}) == 0");
		dtos.add(dto2);

		return dtos;
	}

	FsCellCodeDetailsResponseDto getCellCodeDetails(){
		FsCellCodeDetailsResponseDto dto = new FsCellCodeDetailsResponseDto();
		List<AccountingOemFsCellCode> accountingOemFsCellCodes = new ArrayList<>();
		AccountingOemFsCellCode code1 =  new AccountingOemFsCellCode();
		code1.setOemCode("100");
		code1.setCode("code1");

		AccountingOemFsCellCode code2 =  new AccountingOemFsCellCode();
		code2.setOemCode("200");
		code2.setCode("code2");
		accountingOemFsCellCodes.add(code1);
		accountingOemFsCellCodes.add(code2);
		Map<String, FsCodeDetail> codeVsDetailsMap = Maps.newHashMap();
		codeVsDetailsMap.put("code1", FsCodeDetail.builder().value(BigDecimal.ZERO).build());
		codeVsDetailsMap.put("code2", FsCodeDetail.builder().value(BigDecimal.ONE).build());
		dto.setAccountingOemFsCellCodes(accountingOemFsCellCodes);
		dto.setCodeVsDetailsMap(codeVsDetailsMap);

		return dto;
	}

	File getRulesFile(){
		Map<String, Object[]> data = new TreeMap<String, Object[]>();
		data.put("1", new Object[] {"oem code", "Line", "error code", "type", "page", "description", "expression", "oemId", "year", "country"});
		data.put("2", new Object[] {"2000", "1", "E1", "ERROR", "Page 1", "assets cannot be negative", "(${2000}) >= 0", "GM", 2022, "US"});
		return ExcelFactory.getFile(data);
	}

}
