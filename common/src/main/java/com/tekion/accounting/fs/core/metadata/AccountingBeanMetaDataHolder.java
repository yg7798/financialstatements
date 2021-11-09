package com.tekion.accounting.fs.core.metadata;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Maps;
import com.tekion.core.mysql.lib.annotate.Column;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * This class is dedicated to hold the metadata of beans used in various parts of accounting and can be used to cache more data if needed.
 * and cache the getters and fieldDetails for them
 */
@UtilityClass
@Slf4j
public class AccountingBeanMetaDataHolder {

	private static Map<String, Map<String, Field>> allFields = Maps.newHashMap();

	private static Map<String, Map<String, Method>> classToFieldNameToGetterMethodMap = Maps.newHashMap();

	private static Map<String,Map<String, JsonSerializer>> classToFieldNameToSerializerMap=Maps.newHashMap();

	private static Map<String, Map<String, String>> classToSqlColumnNameToFieldMap = Maps.newHashMap();

	private static Map<String, Map<String, Class<?>>> classToFieldToDataTypeMap = Maps.newHashMap();

	public static Collection<Field> getFields(Class<?> clazz) {
		registerFields(clazz);
		return allFields.get(clazz.getName()).values();
	}

	public static Map<String,Field> getFieldsMap(Class<?> clazz) {
		registerFields(clazz);
		return allFields.get(clazz.getName());
	}

	public static Map<String, String> getSqlColumnNameToFieldMapByClass(Class<?> clazz) {
		registerFields(clazz);
		return classToSqlColumnNameToFieldMap.get(clazz.getName());
	}

	public static Map<String,Class<?>> getFieldToDataTypeMap(Class<?> clazz) {
		registerFields(clazz);
		return classToFieldToDataTypeMap.get(clazz.getName());
	}

	public static Map<String ,JsonSerializer> getSerializerMap(Class<?> clazz) {
		registerFields(clazz);
		return classToFieldNameToSerializerMap.get(clazz.getName());
	}

	public static JsonSerializer getSerializer(String className, String fieldName){
		return classToFieldNameToSerializerMap.get(className) != null ? classToFieldNameToSerializerMap.get(className).get(fieldName) : null;
	}

	public static Method getMethod(String className, String fieldName){
		return classToFieldNameToGetterMethodMap.get(className).get(fieldName);
	}

	public static void registerFields(Class<?> clazz) {
		if(!allFields.containsKey(clazz.getName())) {
			synchronized (allFields) {
				if (!allFields.containsKey(clazz.getName())) {
					Class<?> current = clazz;
					Map<String, Field> fieldsMap = Maps.newHashMap();
					while (current != null) {
						for (Field field : current.getDeclaredFields()) {
							if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
								fieldsMap.put(field.getName(), field);
							}
						}
						current = current.getSuperclass();
					}
					allFields.put(clazz.getName(), fieldsMap);
				}

				if (!classToFieldNameToGetterMethodMap.containsKey(clazz.getName())) {
					Map<String, JsonSerializer> getJsonSerializer = Maps.newHashMap();
					Map<String, Method> getterMethodsMap = Maps.newHashMap();
					Map<String, String> columnNameToFieldMap = Maps.newHashMap();
					Map<String, Class<?>> fieldToDataTypeMap = Maps.newHashMap();
					for (Field field : allFields.get(clazz.getName()).values()) {
						String getterMethod = fieldToGetterMethod(field);
						Class<?> current = clazz;
						while (current != null) {
							try {
								Method method = current.getDeclaredMethod(getterMethod);
								getterMethodsMap.put(field.getName(), method);
								fieldToDataTypeMap.put(field.getName(), field.getType());
								break;
							} catch (NoSuchMethodException e) {
								current = current.getSuperclass();
							}
						}
						for(Annotation annotation:field.getAnnotations())
						{
							if(annotation.annotationType().equals(com.fasterxml.jackson.databind.annotation.JsonSerialize.class))
							{
								com.fasterxml.jackson.databind.annotation.JsonSerialize an = (JsonSerialize) annotation;
								Class<? extends JsonSerializer> using = an.using();
								JsonSerializer jsonSerializer = null;
								try {
									jsonSerializer = using.newInstance();
								} catch (InstantiationException |IllegalAccessException  e) {
									log.error("FATAL ERROR : Serializer no args constructor missing for : {} and FieldName : {} ",clazz.getName(),field.getName());
								}
								getJsonSerializer.put(field.getName(),jsonSerializer);
							}
							if (annotation.annotationType().equals(Column.class)) {
								Column column = (Column) annotation;
								columnNameToFieldMap.put(column.name(), field.getName());
							}
						}
					}

					classToFieldNameToSerializerMap.put(clazz.getName(),getJsonSerializer);
					classToFieldNameToGetterMethodMap.put(clazz.getName(), getterMethodsMap);
					classToSqlColumnNameToFieldMap.put(clazz.getName(), columnNameToFieldMap);
					classToFieldToDataTypeMap.put(clazz.getName(), fieldToDataTypeMap);
				}
			}
		}
	}




	private static String fieldToGetterMethod(Field field) {
		String fieldName = field.getName();
		return (field.getType() == Boolean.TYPE ) ? "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)
				: "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}


}

