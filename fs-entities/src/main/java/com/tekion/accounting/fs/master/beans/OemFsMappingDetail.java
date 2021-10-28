package com.tekion.accounting.fs.master.beans;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OemFsMappingDetail {

	@NotBlank
	private String glAccountId;
	@NotBlank private String fsCellGroupCode;
	private String glAccountDealerId;

}
