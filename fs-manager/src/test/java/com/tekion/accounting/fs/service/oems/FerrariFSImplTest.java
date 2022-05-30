package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class FerrariFSImplTest {


	@InjectMocks
	FerrariServiceImpl ferrariService;

	@Test
	public void testIfUseDownloadFileAPi() {
		FinancialStatementRequestDto dto = new FinancialStatementRequestDto();
		dto.setFileType(OemConfig.SupportedFileFormats.XML.name());
		assertTrue(ferrariService.useDownloadApiFromIntegration(dto));
	}

}
