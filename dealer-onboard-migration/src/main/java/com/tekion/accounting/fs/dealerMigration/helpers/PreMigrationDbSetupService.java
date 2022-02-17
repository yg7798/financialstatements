package com.tekion.accounting.fs.dealerMigration.helpers;

import com.tekion.migration.v3.taskexecutor.MigrationContext;

public interface PreMigrationDbSetupService {
	void clearOemFsMapping(String dealerId, MigrationContext context);
}

