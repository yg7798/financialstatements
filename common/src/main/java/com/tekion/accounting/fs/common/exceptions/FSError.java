package com.tekion.accounting.fs.common.exceptions;

import com.tekion.core.exceptions.TekionError;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum FSError implements TekionError {

	somethingWentWrong("F001","something.went.input"),
	invalidRequest("F002","invalid.request"),
	eitherOemValueTypeOrValueTypeInCellCodeMustPresent("F003","eitherOemValueType.orValueTypeInCellCodeMust.present"),
	invalidPayload("F004", "invalid.payload"),
	invalidOemId("F005","invalid.oem.id"),
	excelSheetNotRegistered("F006","excel.sheets.not.registered"),
	emptyExcelRequestDto("F007","empty.request.dto"),
	duplicateUniqueKey("F008","duplicate.unique.key"),
	dealerDoesNotBelongToTenant("F009", "dealer.doesnt.belong.to.tenant"),
	fsEntryNotFoundById("F010", "fsEntry.id.not.found"),
	internalSystemError("F011","internal.system.error"),
	fsEntryNotFoundForRequestedOemAndYear("F012","fs.entry.not.found"),
	missingGlAccount("F013","glAccount.details.missing.for.dealer"),
	duplicateSiteOemCombinationPresentInRequest("F014","duplicate.site.oem.combination.present.in.request"),
	uploadValidPclCodesFile("F015","upload.valid.PclCodes.file"),
	ioError("F016","io.error"),
	notSupported("F017", "not.supported"),
	valuesExistForRequestedYear("F018","values.exist.for.year"),
	unableToDetermineColumnForReportGeneration("F019","unable.to.determine.column.for.report.generation"),
	entryNumberCannotBeEmpty("F020","entry.number.cannot.empty"),
	invalidFSOemValueType("F021","invalid.oem.value.type"),
	fsSubmitError("F022","fs.submit.error"),
	memoWorksheetsAreMissing("F023", "memo.worksheets.not.present"),
	mediaServiceRequestFailed("F024", "mediaService.request.failed"),
	cellGroupsFoundEmpty("F025", "cellGroups.found.empty"),
	invalidYear("F026","invalid.year"),
	invalidCountry("F027","invalid.country"),
	uploadValidExcelFile("F028","upload.valid.excel.file"),
	;

	FSError(String code, String key) {
		this.code = code;
		this.key = key;
	}


	FSError(String code, String key, String defaultMessage) {
		this.code = code;
		this.key = key;
		this.defaultMessage = defaultMessage;
	}

	private final String code;
	private final String key;
	private String defaultMessage = "unexpected error";
	private static final Map<String, FSError> map = new HashMap<>(values().length, 1);

	public String getCode() {
		return code;
	}

	@Override
	public String getErrorCode() {
		return code;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String getOptionalDebugMessage() {
		return defaultMessage;
	}

	static {
		for (FSError c : values()) map.put(c.code, c);
	}

	public static FSError of(String code) {
		String invalidCode = "Invalid Error Code";

		if(Objects.isNull(code)) throw new IllegalArgumentException(invalidCode);

		FSError result = map.get(code);
		if (Objects.isNull(result)) {
			throw new IllegalArgumentException(invalidCode);
		}
		return result;
	}
}

