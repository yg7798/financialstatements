package com.tekion.accounting.fs.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  CustomFieldType {
	ACCOUNT_SUBTYPE,
	DOCUMENT_TYPE,
	JOURNAL_TYPE,
	FINANCIAL_STATEMENT_GROUP,
	FINANCIAL_STATEMENT_SUB_GROUP,
	PRODUCTIVITY_TYPE,
	UNIT_ANALYSIS_GROUP,

	//migrated separately.
	DEPARTMENT,
	REPAIR_ORDER_INTERNAL,
	REPAIR_ORDER_WARRANTY,
	REPAIR_ORDER_CP_IW_SPLIT,
	PARTS_SALES_ORDER_INTERNAL
}
