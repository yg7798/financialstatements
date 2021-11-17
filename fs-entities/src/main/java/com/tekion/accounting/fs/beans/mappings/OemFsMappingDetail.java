package com.tekion.accounting.fs.beans.mappings;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OemFsMappingDetail {

	@NotBlank
	private String glAccountId;
	@NotBlank private String fsCellGroupCode;
	private String glAccountDealerId;

}
