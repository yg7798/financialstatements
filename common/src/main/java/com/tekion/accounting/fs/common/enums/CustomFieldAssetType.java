package com.tekion.accounting.fs.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomFieldAssetType {
	ACCOUNT("gl_account"),
	JOURNAL("journal_account"),
	COST_CENTRE("cost_center")
	;

	private String displayName;
}
