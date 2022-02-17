package com.tekion.accounting.fs.dealerMigration.postMigrationAssetValidation;

import com.tekion.accounting.fs.dealerMigration.beans.AssetValidationInfo;
import com.tekion.accounting.fs.dealerMigration.enums.AccountingPostMigrationValidator;
import com.tekion.migration.v3.metadata.beans.MigrationTaskName;
import com.tekion.migration.v3.validation.beans.MigrationValidationResult;

import java.util.List;

public interface AbstractPostMigrationValidationExecutor {
	List<MigrationValidationResult> doValidateMigrationTask(AssetValidationInfo assetValidationInfo);

	List<MigrationTaskName> getSupportedTasks();

	AccountingPostMigrationValidator getValidatorName();
}
