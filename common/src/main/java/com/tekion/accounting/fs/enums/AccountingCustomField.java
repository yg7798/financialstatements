package com.tekion.accounting.fs.enums;


import beans.FieldType;
import com.tekion.core.utils.TCollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

import static com.tekion.accounting.fs.enums.CustomFieldType.*;


@AllArgsConstructor
@Getter
public enum AccountingCustomField {
	subtype(CustomFieldAssetType.ACCOUNT.getDisplayName(), "Account Subtype", ACCOUNT_SUBTYPE, FieldType.MULTI_SELECT),
	document(CustomFieldAssetType.JOURNAL.getDisplayName(), "Document Type", DOCUMENT_TYPE, FieldType.MULTI_SELECT),
	journal(CustomFieldAssetType.JOURNAL.getDisplayName(), "Journal Type", JOURNAL_TYPE, FieldType.MULTI_SELECT),
	IB(CustomFieldAssetType.ACCOUNT.getDisplayName(), "Financial Statement Group", FINANCIAL_STATEMENT_GROUP, FieldType.MULTI_SELECT),
	IBS(CustomFieldAssetType.ACCOUNT.getDisplayName(), "Financial Statement Sub Group", FINANCIAL_STATEMENT_SUB_GROUP, FieldType.MULTI_SELECT),
	productivity(CustomFieldAssetType.ACCOUNT.getDisplayName(), "Productivity Type", PRODUCTIVITY_TYPE, FieldType.MULTI_SELECT),
	UNA(CustomFieldAssetType.ACCOUNT.getDisplayName(), "Unit Analysis Group", UNIT_ANALYSIS_GROUP, FieldType.MULTI_SELECT),

	//migrated separately and comes from a different athena table
	DEPT(CustomFieldAssetType.ACCOUNT.getDisplayName(), "Department", DEPARTMENT, FieldType.MULTI_SELECT),

	//cost centre for initializing . ENUM WILL HAVE TO RENAMED IF DATA LOADED IN ATHENA ENUM NAME SHOULD MATCH TYPE LOADED IN ATHENA
	RO_INTERNAL(CustomFieldAssetType.COST_CENTRE.getDisplayName(),"Repair Order Internal",REPAIR_ORDER_INTERNAL,FieldType.MULTI_SELECT),
	PO_INTERNAL(CustomFieldAssetType.COST_CENTRE.getDisplayName(),"Parts Sales Order Internal", PARTS_SALES_ORDER_INTERNAL,FieldType.MULTI_SELECT),
	RO_CP_IW_SPLIT(CustomFieldAssetType.COST_CENTRE.getDisplayName(),"Repair Order CP IW Split",REPAIR_ORDER_CP_IW_SPLIT,FieldType.MULTI_SELECT),
	RO_WARRANTY(CustomFieldAssetType.COST_CENTRE.getDisplayName(),"Repair Order Warranty",REPAIR_ORDER_WARRANTY,FieldType.MULTI_SELECT)

	;

	public static Map<String,AccountingCustomField> keyToCustomFieldMap;
	static {
		keyToCustomFieldMap = TCollectionUtils.nullSafeMap(keyToCustomFieldMap);
		AccountingCustomField[] values = AccountingCustomField.values();
		for (AccountingCustomField value : values) {
			keyToCustomFieldMap.put(value.getKey().name(),value);
		}
	}
	private String assetSubType;
	private String displayName;
	private CustomFieldType key;
	private FieldType fieldType;
}
