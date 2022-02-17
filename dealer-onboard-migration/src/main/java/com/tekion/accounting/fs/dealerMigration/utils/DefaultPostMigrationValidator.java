package com.tekion.accounting.fs.dealerMigration.utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.dealerMigration.beans.AssetValidationInfo;
import com.tekion.accounting.fs.dealerMigration.decisionTaking.MigrationLogicSpecificTask;
import com.tekion.accounting.fs.dealerMigration.decisionTaking.MigrationRuleEngine;
import com.tekion.accounting.fs.dealerMigration.postMigrationAssetValidation.AbstractPostMigrationValidationExecutor;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.migration.v3.metadata.beans.MigrationTaskName;
import com.tekion.migration.v3.validation.beans.MigrationValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.dealerMigration.utils.MigrationValidationV3Utils.getValidationResultForException;
import static org.apache.commons.lang.StringUtils.isBlank;

@Component
@Slf4j
public class DefaultPostMigrationValidator {

	private final Map<String, List<AbstractPostMigrationValidationExecutor>> validationExecutorMap;
	private final Map<String, AbstractPostMigrationValidationExecutor> validatorNameToAbstractTaskMap;
	private boolean loggedValidatorMap = false;
	@Autowired
	private MigrationRuleEngine logicDeciderService;


	public DefaultPostMigrationValidator(List<AbstractPostMigrationValidationExecutor> allSupportedValidators) {
		Set<String> validatorNamesRegistered = new HashSet<>();
		Map<String,AbstractPostMigrationValidationExecutor> validatorNameToExecutorMap = Maps.newHashMap();
		Map<String, List<AbstractPostMigrationValidationExecutor>> validationExecutorMap = Maps.newHashMap();
		for (AbstractPostMigrationValidationExecutor abstractPostMigrationValidationExecutor : TCollectionUtils.nullSafeList(allSupportedValidators)) {
			if(Objects.isNull(abstractPostMigrationValidationExecutor.getValidatorName()) || isBlank(abstractPostMigrationValidationExecutor.getValidatorName().name()) || validatorNamesRegistered.contains(abstractPostMigrationValidationExecutor.getValidatorName().name())){
				throw new UnsupportedOperationException("cannot have 2 validators with the same or blank name  : "+ abstractPostMigrationValidationExecutor.getValidatorName());
			}
			validatorNameToExecutorMap.put(abstractPostMigrationValidationExecutor.getValidatorName().name(),abstractPostMigrationValidationExecutor);
			validatorNamesRegistered.add(abstractPostMigrationValidationExecutor.getValidatorName().name());
			List<MigrationTaskName> supportedTasks = abstractPostMigrationValidationExecutor.getSupportedTasks();
			for (MigrationTaskName migrationTaskName : TCollectionUtils.nullSafeList(supportedTasks)) {
				validationExecutorMap.compute(migrationTaskName.name(), (key, oldValue) -> {
					if (Objects.isNull(oldValue)) {
						oldValue = Lists.newArrayList();
					}
					oldValue.add(abstractPostMigrationValidationExecutor);
					return oldValue;
				});
			}
		}
		this.validationExecutorMap = validationExecutorMap;
		this.validatorNameToAbstractTaskMap = validatorNameToExecutorMap;
		logValidatorMapInFirstRun();
		loggedValidatorMap =false;
	}


	public List<MigrationValidationResult> doPerformAssetValidation(List<MigrationTaskName> supportedTaskList, AssetValidationInfo validationInfo) {
		logValidatorMapInFirstRun();
		Set<String> tasksNotValidated = new HashSet<>();
		List<MigrationValidationResult> validationResults = Lists.newArrayList();
		for (MigrationTaskName migrationTaskName : TCollectionUtils.nullSafeList(supportedTaskList)) {
			if (!validationExecutorMap.containsKey(migrationTaskName.name())) {
				tasksNotValidated.add(migrationTaskName.name());
			} else {
				List<AbstractPostMigrationValidationExecutor> abstractPostMigrationValidationExecutors = validationExecutorMap.get(migrationTaskName.name());
				for (AbstractPostMigrationValidationExecutor abstractPostMigrationValidationExecutor : abstractPostMigrationValidationExecutors) {
					doValidate(supportedTaskList, validationInfo, validationResults, abstractPostMigrationValidationExecutor, migrationTaskName.name());
				}
			}
		}

		log.info("following tasks were not validated : {}", tasksNotValidated.toString());
		return validationResults;
	}

	private void doValidate(List<MigrationTaskName> supportedTaskList, AssetValidationInfo validationInfo, List<MigrationValidationResult> validationResults, AbstractPostMigrationValidationExecutor abstractPostMigrationValidationExecutor, String name) {
		try {
			log.info("starting validation for tasks : {} , validatorName :{}", supportedTaskList, abstractPostMigrationValidationExecutor.getValidatorName());
			validationResults.addAll(TCollectionUtils.nullSafeList(abstractPostMigrationValidationExecutor.doValidateMigrationTask(validationInfo)));
		} catch (Exception e) {
			validationResults.add(getValidationResultForException(e, validationInfo.getTaskId(), name, abstractPostMigrationValidationExecutor.getValidatorName().name()));
			log.info("Error while performing migration validation using validator : {} ", abstractPostMigrationValidationExecutor.getValidatorName());
			log.error("failed due to error ", e);
//          log.debug("stack trace for validator : {} : {}",abstractMigrationValidationExecutor.getValidatorName(),e.getStackTrace());
		}
	}



	public List<MigrationValidationResult> doPerformAssetValidation(List<MigrationTaskName> supportedTaskList,
																	AssetValidationInfo validationInfo,
																	MigrationLogicSpecificTask migrationLogicSpecificTask) {
		List<MigrationValidationResult> validationResults = Lists.newArrayList();
		if(logicDeciderService.performFlagAndDmsSpecificTask(migrationLogicSpecificTask,validationInfo.getTaskId())){
			validationResults.addAll(doPerformAssetValidation(supportedTaskList,validationInfo));
		}
		else {
			Set<String> tasksNotValidated = new HashSet<>();
			for (MigrationTaskName migrationTaskName : supportedTaskList) {
				tasksNotValidated.add(migrationTaskName.name());
			}
			log.info("following tasks were not allow to validated : {}", tasksNotValidated.toString());
		}
		return validationResults;
	}


	private void logValidatorMapInFirstRun() {
		if(!loggedValidatorMap){
			HashMap<String, List<String>> loggingMap = Maps.newHashMap();
			for (Map.Entry<String, List<AbstractPostMigrationValidationExecutor>> stringListEntry : validationExecutorMap.entrySet()) {
				List<String> validatorsForMigrationTask = stringListEntry.getValue().stream().map(a->a.getValidatorName().name()).collect(Collectors.toList());
				loggingMap.put(stringListEntry.getKey(),validatorsForMigrationTask);
			}
			log.info("VALIDATOR LIST : Migration validations put in place  : {}",loggingMap);
			loggedValidatorMap = true;
		}
	}
}

