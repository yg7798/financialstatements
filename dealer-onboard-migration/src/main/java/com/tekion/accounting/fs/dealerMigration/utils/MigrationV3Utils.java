package com.tekion.accounting.fs.dealerMigration.utils;

import com.tekion.accounting.fs.dealerMigration.beans.ClassFieldsDatatypeAndName;
import com.tekion.accounting.fs.dealerMigration.fieldMetaData.AbstractFieldMetaData;
import com.tekion.core.utils.TStringUtils;
import com.tekion.migration.v3.preview.EntityRowData;
import com.tekion.migration.v3.preview.MigrationPreviewEntity;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

@UtilityClass
@Slf4j
public class MigrationV3Utils {

	public static <I extends Enum<I> & ClassFieldsDatatypeAndName, R> R transformToAthenaRow(
			MigrationPreviewEntity migrationPreviewEntity,
			Class<R> objectClass,
			List<AbstractFieldMetaData> fieldMetaDataList)
			throws IllegalAccessException, InstantiationException, NoSuchMethodException {

		EntityRowData entityRowData = migrationPreviewEntity.getRows().get(0);
		R athenaObject = objectClass.newInstance();
		for (AbstractFieldMetaData fieldMetaData : fieldMetaDataList) {
			String fieldName = fieldMetaData.getBeanFieldName();
			if(entityRowData.getTransformedRowAData().containsKey(fieldName)){
				String varCharData = entityRowData.getColumnValue(fieldName);
				String setterName = getSetterName(fieldName);
				Class<?> clazz = fieldMetaData.getClazz();

				Object data = null;
				if (String.class == clazz) {
					if (varCharData == null) {
						data = "";
					} else {
						data = varCharData;
					}
				} else if (Boolean.class == clazz) {
					data = Boolean.valueOf(varCharData);
					// Convert and Process com.tekion.as boolean

				} else if (Integer.class == clazz) {
					if (TStringUtils.isBlank(varCharData)) {
						data = 0;
					} else {
						data = Integer.valueOf(varCharData);
					}

				} else if (Double.class == clazz) {
					if (TStringUtils.isBlank(varCharData)) {
						data = 0.0;
					} else {
						data = Double.valueOf(varCharData);
					}
				} else {
					throw new RuntimeException(
							"Unexpected Type is not expected " + clazz.toString());
				}
				Method m = objectClass.getMethod(Objects.requireNonNull(setterName), clazz);
				try {
					m.invoke(athenaObject, data);
				} catch (Exception e) {
					log.error("error occured while casting : ",e);
				}
			}
		}

		return athenaObject;
	}

	private static String getSetterName(String fieldName) {
		return "set" + StringUtils.capitalize(fieldName);
	}

}

