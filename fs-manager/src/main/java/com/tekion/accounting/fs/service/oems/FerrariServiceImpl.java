package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component

public class FerrariServiceImpl extends AbstractFinancialStatementService {
	public FerrariServiceImpl(DealerConfig dealerConfig, IntegrationClient integrationClient, FsXMLServiceImpl fsXMLService) {
		super(dealerConfig, integrationClient, fsXMLService);
	}

	/**
	 * This is used to call separate API from integration if file type is XML for Ferrari OEM
	 *  see https://tekion.atlassian.net/browse/CDMS-45328
	 * */
	boolean useDownloadApiFromIntegration(FinancialStatementRequestDto requestDto){
		return OemConfig.SupportedFileFormats.XML.name().equalsIgnoreCase(requestDto.getFileType());
	}
}
