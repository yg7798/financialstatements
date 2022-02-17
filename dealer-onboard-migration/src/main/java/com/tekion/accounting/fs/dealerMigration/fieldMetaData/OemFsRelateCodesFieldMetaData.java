package com.tekion.accounting.fs.dealerMigration.fieldMetaData;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static java.util.Collections.emptyList;

@Getter
@AllArgsConstructor
public enum OemFsRelateCodesFieldMetaData implements AbstractFieldMetaData {

	DMS_ACCOUNT_NUMBER("dmsAccountNumber", "dms_account_number", emptyList(), true, true, 1, String.class),
	FS_STATEMENT_NUMBER("fsStatementNumber","financial_statement_number",emptyList(),true,true,1,String.class),
	OEM_ACCOUNT_NUMBER("oemAccountNumber", "oem_account_number", emptyList(), false, false, 1, String.class),
	PCL_CODE("pclCode", "pcl_code", emptyList(), false, false, 1, String.class),

	OEM_STATEMENT_FORMAT("oemStatementFormat", "oem_statement_format", emptyList(), true, true, 1, String.class),
	OEM_STATEMENT_VERSION("oemStatementVersion", "oem_statement_version", emptyList(), true, true, 1, String.class),
	SITE_ID("siteId","site_id",emptyList(), true, true, 1, String.class)
	;

	private final String beanFieldName;
	private final String athenaFieldName;
	private final List<String> alternateAthenaFieldNames;
	private final boolean mandatory;
	private final boolean warnIfAbsent;
	private final int warnLevel;
	private final Class<?> clazz;
}

