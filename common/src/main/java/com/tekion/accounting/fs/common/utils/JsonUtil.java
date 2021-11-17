package com.tekion.accounting.fs.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.tekion.core.excelGeneration.models.utils.TCollectionUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@UtilityClass
public class JsonUtil {
	private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
	public static final ObjectMapper MAPPER = new ObjectMapper();

	public static String toJson(Object obj) {
		ObjectMapper objectMapper = MAPPER;
		String json = StringUtils.EMPTY;
		try {
			json = objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			logger.error("error while converting java object to json string ", e);
		}
		return json;
	}

	public static <T> Optional<T> fromJson(String json, Class<T> clazz) {
		ObjectMapper objectMapper = MAPPER;
		try {
			return Optional.ofNullable(objectMapper.readValue(json, clazz));
		} catch (IOException e) {
			logger.error("error while creating object of {} ", clazz.getName(), e);
		}
		return Optional.empty();
	}
	public static <T> T initializeFromJson(String json, Class<T> clazz){
		return JsonUtil.fromJson(json, clazz).orElse(null);
	}

	public static <T> T initializeFromJson(String json, TypeReference<T> typeReference){
		return JsonUtil.fromJson(json, typeReference).orElse(null);
	}

	public static <T> Optional<T> fromJsonIgnoreUnknown(String json, Class<T> clazz) {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			return Optional.ofNullable(objectMapper.readValue(json, clazz));
		} catch (IOException e) {
			logger.error("error while creating object of %s", clazz.getName());
		}
		return Optional.empty();
	}

	public static <T> Optional<T> fromJson(String json, Type type) {
		ObjectMapper objectMapper = MAPPER;
		try {
			JavaType javaType = TypeFactory.defaultInstance().constructType(type);
			return Optional.ofNullable(objectMapper.readValue(json, javaType));
		} catch (IOException e) {
			logger.error("error while creating object");
		}
		return Optional.empty();
	}

	public static <T> Optional<T> fromJson(String json, TypeReference<T> typeReference) {
		ObjectMapper objectMapper = MAPPER;
		try {
			return Optional.ofNullable(objectMapper.readValue(json, typeReference));
		} catch (IOException e) {
			logger.error("error while creating object");
		}
		return Optional.empty();
	}

	public static <T> T fromJsonNode(JsonNode jsonNode, Class<T> clazz) {
		return MAPPER.convertValue(jsonNode, clazz);
	}

	public static boolean isNullNode(JsonNode jsonNode){
		return (jsonNode == null || jsonNode.isNull());
	}

	public static <I,R> R convertToTargetClassObj(I inputObject, Class<R> targetClass){
		if(Objects.isNull(inputObject)){
			return (R) null;
		}
		return JsonUtil.fromJson(JsonUtil.toJson(inputObject), targetClass).orElse(null);
	}

	public static <I,T> List<T> convertToTargetClassList(List<I> inputList, Class<T> targetClass){
		if(Objects.isNull(inputList)){
			return null;
		}
		if(TCollectionUtils.isEmpty(inputList)){
			return new ArrayList<T>();
		}

		List<T> listOfCastedObjects = Lists.newArrayList();

		for (I i : inputList) {
			listOfCastedObjects.add(convertToTargetClassObj(i,targetClass));
		}
		return listOfCastedObjects;
	}
}
