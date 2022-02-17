package com.tekion.accounting.fs.dealerMigration.beans;

import org.springframework.lang.NonNull;

public interface ClassFieldsDatatypeAndName {
	@NonNull
	Class<?> getDataType();

	@NonNull
	String getFieldName();

	@NonNull
	String getFieldSetterName();
}
