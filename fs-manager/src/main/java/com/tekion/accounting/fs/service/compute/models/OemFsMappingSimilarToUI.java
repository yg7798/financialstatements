package com.tekion.accounting.fs.service.compute.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tekion.accounting.commons.utils.LocaleUtils;
import com.tekion.accounting.fs.service.common.cache.dtos.OptionMinimal;
import com.tekion.accounting.fs.common.enums.CustomFieldType;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.as.models.beans.TrialBalanceRow;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.util.Lists;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.tekion.accounting.fs.common.enums.CustomFieldType.DEPARTMENT;
import static com.tekion.core.utils.TStringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class OemFsMappingSimilarToUI {

	private String status;
	private String glNumber;
	private String glName;
	private String accountStatus;
	private String accountType;
	private String department;
	private BigDecimal ytdBalance = BigDecimal.ZERO;
	private BigDecimal mtdBalance = BigDecimal.ZERO;
	private Long mtdCount;
	private Long ytdCount;
	private List<String> groupCodes;

	public static final String MAPPED_KEY = "key.mapped";
	public static final String UNMAPPED_KEY = "key.unmapped";
	public static final String ACTIVE_KEY = "key.active";
	public static final String INACTIVE_KEY = "key.inactive";

	public void toOemFsMappingBean(TrialBalanceRow trialBalanceRow, GLAccount glAccount, Map<String,List<String>> idOemFsMappings){
		this.setGlNumber(glAccount.getAccountNumber());
		this.setGlName(glAccount.getAccountName());
		this.setAccountStatus(glAccount.isActive()? LocaleUtils.translateLabel(ACTIVE_KEY)
				: LocaleUtils.translateLabel(INACTIVE_KEY));
		this.setAccountType(trialBalanceRow.getAccountType());
		this.setDepartment("");
		this.setYtdBalance(trialBalanceRow.getCurrentBalance());
		this.setMtdBalance(trialBalanceRow.getCurrentBalance().subtract(trialBalanceRow.getOpeningBalance()));
		this.setMtdCount(trialBalanceRow.getCount());
		this.setYtdCount(trialBalanceRow.getYtdCount());
		this.setGroupCodes(idOemFsMappings.get(glAccount.getId()));
		if(idOemFsMappings.containsKey(glAccount.getId()) && idOemFsMappings.get(glAccount.getId()).size()>0) {
			this.setStatus(LocaleUtils.translateLabel(MAPPED_KEY));
			this.setGroupCodes(idOemFsMappings.get(glAccount.getId()));
		}else {
			this.setStatus(LocaleUtils.translateLabel(UNMAPPED_KEY));
			this.setGroupCodes(Lists.emptyList());
		}
	}

	public void resolveDepartmentFromId(GLAccount glAccount, Map<CustomFieldType, Map<String, OptionMinimal>> keyToIdToOptionMap){
		if(isNotBlank(glAccount.getDepartmentId())){
			String optionDisplayLabel = getOptionDisplayLabelWithoutCode(DEPARTMENT, glAccount.getDepartmentId(), "", keyToIdToOptionMap);
			this.setDepartment(isBlank(optionDisplayLabel)? "" : optionDisplayLabel);
		}
	}


	public static String getOptionDisplayLabelWithoutCode(CustomFieldType key, String id, String placeHolder,Map<CustomFieldType, Map<String, OptionMinimal>> keyToIdToOptionMap){
		if (keyToIdToOptionMap.containsKey(key)) {
			if (keyToIdToOptionMap.get(key).containsKey(id)) {
				return keyToIdToOptionMap.get(key).get(id).getName();
			}
		}
		return placeHolder;
	}
}

