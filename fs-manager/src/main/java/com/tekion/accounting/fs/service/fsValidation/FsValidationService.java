package com.tekion.accounting.fs.service.fsValidation;

import com.tekion.accounting.fs.beans.fsValidation.FsValidationRule;
import com.tekion.accounting.fs.dto.fsValidation.FsValidationResult;
import com.tekion.accounting.fs.dto.fsValidation.FsValidationRuleDto;
import com.tekion.accounting.fs.enums.OEM;

import java.util.List;

public interface FsValidationService {
	List<FsValidationRule> importRules(String mediaId, String oemId, String country, Integer year);
	List<FsValidationResult> validateFs(String fsId, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances);
	Integer copyRules(String oemId, String country, Integer fromYear, Integer toYear);
	List<FsValidationRule> getRules(OEM oem, Integer year, String country);
	List<FsValidationRule> save(List<FsValidationRuleDto> rules);
	void delete(List<String> ids);
	void deleteAll(String oemId, Integer year, String country);
}
