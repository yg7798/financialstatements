package com.tekion.accounting.fs.dealerMigration.fieldMetaData;

import java.util.List;

public interface AbstractFieldMetaData {
	String getAthenaFieldName();
	String getBeanFieldName();
	List<String> getAlternateAthenaFieldNames();
	boolean isMandatory();
	boolean isWarnIfAbsent();
	int getWarnLevel();
	Class<?> getClazz();
	default String getDefaultValue(){
		return null;
	}
}
