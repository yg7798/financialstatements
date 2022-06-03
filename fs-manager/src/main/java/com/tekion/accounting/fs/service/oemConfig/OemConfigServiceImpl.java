package com.tekion.accounting.fs.service.oemConfig;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.accountingInfo.FSPreferences;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.dto.oemConfig.OemConfigRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.OemConfigRepo;
import com.tekion.accounting.fs.service.utils.FSPreferencesUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class OemConfigServiceImpl implements OemConfigService{

	private final OemConfigRepo oemConfigRepo;
	private final DealerConfig dealerConfig;

	@Override
	public OemConfig getOemConfig(String oemId) {
		return oemConfigRepo.findByOemId(oemId, dealerConfig.getDealerCountryCode());
	}

	@Override
	public OemConfig saveOemConfig(OemConfigRequestDto requestDto) {
		OemConfig oemConfigInDb = oemConfigRepo.findByOemId(requestDto.getOemId().name(), requestDto.getCountry());
		if (Objects.nonNull(oemConfigInDb)) {
			oemConfigInDb.setCountry(requestDto.getCountry());
			oemConfigInDb.setXmlEnabled(requestDto.isXmlEnabled());
			oemConfigInDb.setOemLogoURL(requestDto.getOemLogoURL());
			oemConfigInDb.setSubmissionEnabled(requestDto.isSubmissionEnabled());
			oemConfigInDb.setDefaultPrecision(requestDto.getDefaultPrecision());
			oemConfigInDb.setSupportedFileFormats(requestDto.getSupportedFileFormats().stream().map(OemConfig.SupportedFileFormats::name).collect(Collectors.toList()));
			oemConfigInDb.setUseDealerLogo(requestDto.isUseDealerLogo());
			oemConfigInDb.setAdditionalInfo(requestDto.getAdditionalInfo());
			oemConfigInDb.setDownloadFileFromIntegration(requestDto.isDownloadFileFromIntegration());
			oemConfigInDb.setEnableRoundOff(requestDto.isEnableRoundOff());
			oemConfigInDb.setEnableRoundOffOffset(requestDto.isEnableRoundOffOffset());
			oemConfigInDb.setModifiedByUserId(UserContextProvider.getCurrentUserId());
			oemConfigInDb.setModifiedTime(System.currentTimeMillis());
			oemConfigInDb.setFsValidationEnabled(requestDto.isFsValidationEnabled());
			return oemConfigRepo.save(oemConfigInDb);

		} else {
			OemConfig oemConfigToBeSaved = requestDto.createOemInfo();
			return oemConfigRepo.save(oemConfigToBeSaved);
		}
	}

	@Override
	public void enableRoundedTrialBal(OEM oem, String country) {
		OemConfig oemConfig = oemConfigRepo.findByOemId(oem.name(), country);
		FSPreferences fsPreferences = oemConfig.getFsPreferences();
		if (oemConfig.getFsPreferences() == null) {
			fsPreferences = new FSPreferences();
			oemConfig.setFsPreferences(fsPreferences);
		}

		FSPreferencesUtils.enableRoundedTrialBal(fsPreferences,  oem.name());
		oemConfig.setModifiedTime(System.currentTimeMillis());
		oemConfigRepo.save(oemConfig);
	}

	@Override
	public void disableRoundedTrialBal(OEM oem, String country) {
		OemConfig oemConfig = oemConfigRepo.findByOemId(oem.name(), country);
		FSPreferences fsPreferences = oemConfig.getFsPreferences();
		if(oemConfig.getFsPreferences() == null){
			return;
		}
		FSPreferencesUtils.disableRoundedTrialBal(fsPreferences, oem.name());
		oemConfig.setModifiedTime(System.currentTimeMillis());
		oemConfigRepo.save(oemConfig);
	}
}
