package com.tekion.accounting.fs.enums;

import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.core.exceptions.TBaseRuntimeException;

import java.util.Objects;

public enum OemValueType {
	VALUE,
	UNIT1,
	UNIT2,
	BALANCE1,
	BALANCE2;

	public static void validate(String name){
		if(Objects.isNull(name)) return;
		try{
			OemValueType.valueOf(name.toUpperCase());
		}catch (IllegalArgumentException e){
			throw new TBaseRuntimeException(FSError.invalidFSOemValueType);
		}
	}
}
