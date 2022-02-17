package com.tekion.accounting.fs.dealerMigration.utils;

import com.google.common.collect.Maps;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.migration.v3.customPreValidation.beans.MigrationCustomPreValidationResult;
import com.tekion.migration.v3.validation.beans.MigrationValidationResult;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

import static com.tekion.migration.v3.validation.beans.MigrationValidationResult.builder;

@UtilityClass
public class MigrationValidationV3Utils {
	public static final String GENERIC_VALIDATION_TASK= "Validation Task Sequence";

	public static final String VALIDATION_TASK__THREW_EXCEPTION = "Migration validation failed to perform : Task Name : %s ValidationName : %s   ";

	private static MigrationValidationResult getBasicValidationResult(String name, MigrationValidationResult.ValidationResult validationResult, String message, String taskId){
		return builder()
				.message(message)
				.validationResult(validationResult)
				.name(name)
				.taskId(taskId)
				.build();
	}
	private static MigrationValidationResult getBasicValidationResult(String name, MigrationValidationResult.ValidationResult validationResult, String message, String taskId, Map<String, String> additionalInfo){
		return builder()
				.message(message)
				.validationResult(validationResult)
				.name(name)
				.taskId(taskId)
				.additionalInfo(additionalInfo)
				.build();
	}

	public static MigrationValidationResult getValidationResultForException(Exception e, String taskId, String migrationTaskName, String validatorName){
		MigrationValidationResult migrationValidationResult = new MigrationValidationResult();
		migrationValidationResult.setName(GENERIC_VALIDATION_TASK);
		migrationValidationResult.setMessage(String.format(VALIDATION_TASK__THREW_EXCEPTION,migrationTaskName,validatorName));
		migrationValidationResult.setTaskId(taskId);
		HashMap<String, String> additionalInfoMap = Maps.newHashMap();
		additionalInfoMap.put("Exception Stack Trace",e.getStackTrace().toString());
		return migrationValidationResult;
	}

	public static MigrationCustomPreValidationResult getPreValidationResultForException(Exception e, String taskId, String migrationTaskName, String validatorName){
		MigrationCustomPreValidationResult migrationValidationResult = new MigrationCustomPreValidationResult();
		migrationValidationResult.setName(GENERIC_VALIDATION_TASK);
		migrationValidationResult.setMessage(String.format(VALIDATION_TASK__THREW_EXCEPTION,migrationTaskName,validatorName));
		migrationValidationResult.setTaskId(taskId);
		HashMap<String, String> additionalInfoMap = Maps.newHashMap();
		additionalInfoMap.put("Exception Stack Trace",e.getStackTrace().toString());
		return migrationValidationResult;
	}
}

