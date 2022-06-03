package com.tekion.accounting.fs.service.oemConfig;

import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.dto.oemConfig.OemConfigRequestDto;
import com.tekion.accounting.fs.enums.OEM;


public interface OemConfigService {

	OemConfig getOemConfig(String oemId);

	OemConfig saveOemConfig(OemConfigRequestDto requestDto);

	void enableRoundedTrialBal(OEM oem, String country);

	void disableRoundedTrialBal(OEM oem, String country);
}
