package com.tekion.accounting.fs.dealerMigration.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AssetValidationInfo {
	private String tenantId;
	private String dealerId;
	private String migrationId;
	private String taskId;
	private String taskName;
	private String countQuery;

}
