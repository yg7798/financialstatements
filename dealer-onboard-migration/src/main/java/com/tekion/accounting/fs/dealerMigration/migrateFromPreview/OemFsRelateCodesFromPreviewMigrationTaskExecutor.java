package com.tekion.accounting.fs.dealerMigration.migrateFromPreview;

import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.dealerMigration.beans.AssetValidationInfo;
import com.tekion.accounting.fs.dealerMigration.context.OemFsRelateCodesMigrationContext;
import com.tekion.accounting.fs.dealerMigration.decisionTaking.MigrationRuleEngine;
import com.tekion.accounting.fs.dealerMigration.helpers.MigrationAssetValidationHelperService;
import com.tekion.accounting.fs.dealerMigration.helpers.PreMigrationDbSetupService;
import com.tekion.accounting.fs.dealerMigration.rowTransaformer.OemFsRelateCodesFieldRowTransformer;
import com.tekion.accounting.fs.dealerMigration.utils.DefaultPostMigrationValidator;
import com.tekion.accounting.fs.dealerMigration.utils.MigrationV3Constants;
import com.tekion.accounting.fs.enums.AccountType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.migration.v3.dealerdata.DealerMigrationDataRepository;
import com.tekion.migration.v3.metadata.MigrationEntityMetadataService;
import com.tekion.migration.v3.metadata.beans.MigrationMetadata;
import com.tekion.migration.v3.metadata.beans.MigrationName;
import com.tekion.migration.v3.metadata.beans.MigrationTaskName;
import com.tekion.migration.v3.preview.PreviewEntityRepo;
import com.tekion.migration.v3.task.MigrationTaskRepoV3;
import com.tekion.migration.v3.taskexecutor.AbstractFromPreviewMigrationTaskExecutor;
import com.tekion.migration.v3.taskexecutor.MigrationThreadPool;
import com.tekion.migration.v3.taskexecutor.RowTransformer;
import com.tekion.migration.v3.validation.beans.MigrationValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.dealerMigration.decisionTaking.MigrationLogicSpecificTask.ALLOW_CLEARING_FS_OEM_MAPPING;
import static com.tekion.migration.v3.metadata.beans.MigrationTaskName.ACCOUNTING_FS_CELL_CODE_MIGRATE_FROM_PREVIEW;

@Slf4j
@Component
public class OemFsRelateCodesFromPreviewMigrationTaskExecutor extends AbstractFromPreviewMigrationTaskExecutor<OemFsRelateCodesMigrationContext, OemFsMapping> {


	@Autowired
	private MigrationAssetValidationHelperService helperService;
	@Autowired
	private OemFsRelateCodesFieldRowTransformer fsRelateCodesFieldRowTransformer;
	@Autowired
	private OemFSMappingRepo oemFSMappingRepo;
	@Autowired
	private PreMigrationDbSetupService migrationDbSetupService;
	@Autowired
	private FsEntryService fsEntryService;
	@Autowired
	private FSEntryRepo fsEntryRepo;
	@Autowired
	private MigrationRuleEngine logicDeciderService;
	@Autowired
	private AccountingService accountingService;
	@Autowired
	private DefaultPostMigrationValidator migrationValidator;



	public OemFsRelateCodesFromPreviewMigrationTaskExecutor(MigrationEntityMetadataService entityMetadataService,
															MigrationTaskRepoV3 taskRepo,
															PreviewEntityRepo previewEntityRepo,
															MigrationThreadPool migrationExecutorService,
															DealerMigrationDataRepository dealerMigrationDataRepository) {
		super(entityMetadataService, taskRepo, previewEntityRepo, migrationExecutorService, dealerMigrationDataRepository);
	}

	@Override
	protected RowTransformer<OemFsMapping, OemFsRelateCodesMigrationContext> getRowTransformer() {
		return fsRelateCodesFieldRowTransformer;
	}


	@Override
	protected int processTransformedRows(OemFsRelateCodesMigrationContext context, List<OemFsMapping> list) {
		List<OemFsMapping> mappingToInsert =
				TCollectionUtils.nullSafeList(list).stream().filter(item -> Objects.nonNull(item)).collect(Collectors.toList());
		oemFSMappingRepo.updateBulk(mappingToInsert);

		return mappingToInsert.size();
	}

	@Override
	protected OemFsRelateCodesMigrationContext createMigrationContext() {
		return doCreateMigrationContext();
	}

	@Override
	public List<MigrationTaskName> supportedMigrationTasks() {
		return Arrays.asList(ACCOUNTING_FS_CELL_CODE_MIGRATE_FROM_PREVIEW);
	}

	@Override
	public List<MigrationValidationResult> validateTaskOutput(String s, String s1, String s2, String s3, String s4) {
		AssetValidationInfo assetValidationInfo = helperService.getAssetValidationInfo(s, s1, s2, s3, s4);
		return migrationValidator.doPerformAssetValidation(supportedMigrationTasks(), assetValidationInfo);
	}

	@Override
	public void clearMigrationData(String tenantId, String dealerId, Map<String,String> additionalInfo) {
		if(logicDeciderService.performFlagAndDmsSpecificTask(ALLOW_CLEARING_FS_OEM_MAPPING,tenantId,dealerId,additionalInfo)){
			log.info("clearing data : oemFsMapping : started");
			migrationDbSetupService.clearOemFsMapping(dealerId,null);
			log.info("clearing data : oemFsMapping : completed");
		}
		else{
			log.info("didnt clear oemFsMapping. Overriding Flag not found or operation not permitted for the following dms");
		}

		MigrationMetadata migMetaData = entityMetadataService.findByMigrationNameAndDealerAndTenantId(tenantId, dealerId, MigrationName.ACCOUNTING_FINANCIAL_STATEMENTS.name());


		String tkOemId = migMetaData.getAdditionalInfo().get(MigrationV3Constants.tkOemId);
		Integer tkOemYear = Integer.parseInt(migMetaData.getAdditionalInfo().get(MigrationV3Constants.tkOemYear));
		Integer tkOemVersion = Integer.parseInt(migMetaData.getAdditionalInfo().get(MigrationV3Constants.tkOemVersion));
		//migrationDbSetupService.clearOemFsRelateCodesGlMappings(dealerId, tkOemId, tkOemYear, tkOemVersion, null);
	}

	private OemFsRelateCodesMigrationContext doCreateMigrationContext() {
		OemFsRelateCodesMigrationContext context = new OemFsRelateCodesMigrationContext();
		context.setMigrationTime(System.currentTimeMillis());
		List<String> accountTypes = new ArrayList<String>() {{
			add(AccountType.MEMO.name());
			add(AccountType.EXPENSE_ALLOCATION.name());
		}};

		List<GLAccount> tekionGlAccounts = TCollectionUtils.nullSafeCollection(accountingService.getGLAccounts(UserContextProvider.getCurrentDealerId()));
		tekionGlAccounts = tekionGlAccounts.stream().filter(x -> (AccountType.MEMO.name().equals(x.getAccountName())))
				.filter(y -> AccountType.EXPENSE_ALLOCATION.name().equals(y.getAccountName())).collect(Collectors.toList());

		//List<GLAccount> tekionGlAccounts = accountRepository.findAllGlAccountsExceptAccountsHavingAccountTypeGivenInList(UserContextProvider.getCurrentDealerId(),accountTypes);
		Map<String, String> accountNoToIdMap = tekionGlAccounts.stream().collect(Collectors.toMap(GLAccount::getAccountNumber, GLAccount::getId));
		context.setAccountNumberToAccountIdMap(accountNoToIdMap);

		List<OemFsMapping> oemFsMappings = oemFSMappingRepo.findAllByDealerIdIncludingDeleted();
		Map<String,OemFsMapping> universalIdToOemFsMap = TCollectionUtils.transformToMap(oemFsMappings,OemFsMapping::getUniversalId);
		context.setUniversalIdToOemFsMap(universalIdToOemFsMap);

		context.setFsEntryRepo(fsEntryRepo);
		context.setFsEntryService(fsEntryService);

		return context;
	}
}

