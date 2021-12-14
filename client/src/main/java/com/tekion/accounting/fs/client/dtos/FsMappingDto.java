package com.tekion.accounting.fs.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FsMappingDto {
	private String oemId;
	private Integer year;
	private Integer version;
	private String fsId;
	private String glAccountId;
	private String glAccountDealerId;
	private String fsCellGroupCode;
	private String dealerId;
	private String siteId;
	private String createdByUserId;
	private String modifiedByUserId;
	private boolean migrated;
	private Long migratedTime;
}
