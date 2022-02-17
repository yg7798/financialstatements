package com.tekion.accounting.fs.dealerMigration.createPreview;

import com.tekion.migration.v3.dealerdata.DealerMigrationDataRepository;
import com.tekion.migration.v3.metadata.MigrationEntityMetadataService;
import com.tekion.migration.v3.metadata.beans.MigrationTaskName;
import com.tekion.migration.v3.preview.PreviewEntityRepo;
import com.tekion.migration.v3.task.MigrationTaskRepoV3;
import com.tekion.migration.v3.taskexecutor.AbstractAthenaPreviewTaskExecutor;
import com.tekion.migration.v3.taskexecutor.MigrationContext;
import com.tekion.migration.v3.taskexecutor.MigrationThreadPool;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OemFsRelateCodesAthenaPreviewTaskExecutor extends AbstractAthenaPreviewTaskExecutor<MigrationContext> {

	protected OemFsRelateCodesAthenaPreviewTaskExecutor(PreviewEntityRepo previewEntityRepo, MigrationEntityMetadataService entityMetadataService, MigrationTaskRepoV3 taskRepo, MigrationThreadPool executorService, DealerMigrationDataRepository dealerMigrationDataRepository) {
		super(previewEntityRepo, entityMetadataService, taskRepo, executorService, dealerMigrationDataRepository);
	}

	@Override
	protected MigrationContext createMigrationContext() {
		return new MigrationContext();
	}

	@Override
	public List<MigrationTaskName> supportedMigrationTasks() {
		return Arrays.asList(
				MigrationTaskName.ACCOUNTING_FS_CELL_CODE_CREATE_PREVIEW
		);
	}

}
