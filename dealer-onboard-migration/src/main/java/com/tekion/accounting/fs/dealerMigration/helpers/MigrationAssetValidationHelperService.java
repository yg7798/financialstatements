package com.tekion.accounting.fs.dealerMigration.helpers;

import com.tekion.accounting.fs.dealerMigration.beans.AssetValidationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationAssetValidationHelperService {

	public AssetValidationInfo getAssetValidationInfo(String tenantId, String dealerId, String migrationId, String taskId, String taskName){
		return AssetValidationInfo.builder()
				.dealerId(dealerId)
				.migrationId(migrationId)
				.taskId(taskId)
				.taskName(taskName)
				.tenantId(tenantId)
				.build();
	}
}
