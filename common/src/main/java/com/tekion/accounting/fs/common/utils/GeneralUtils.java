package com.tekion.accounting.fs.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.as.models.beans.Money;
import com.tekion.core.properties.TekionCommonProperties;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import joptsimple.internal.Strings;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class GeneralUtils {

	private static Pattern alphanumericPattern = Pattern.compile("[a-zA-Z0-9]*");

	public static Map<String, String> internalCallHeaderMap(@Nullable Map<String, String> userHeaderOptions) {
		Map<String, String> headerMap = new HashMap();
		headerMap.put("dealerId".toLowerCase(), UserContextProvider.getCurrentDealerId());
		headerMap.put("userId".toLowerCase(), UserContextProvider.getCurrentUserId());
		headerMap.put("tenantId".toLowerCase(), UserContextProvider.getCurrentTenantId());
		headerMap.put("Content-Type", "application/json");
//    headerMap.put("requestId".toLowerCase(), UserContextProvider.getContext());
		headerMap.put("tekion-api-token", UserContextProvider.getDSEApiToken());
//    headerMap.put("tekion-otp", UserContextProvider.getTekionOtp());
//    headerMap.put("correlationId".toLowerCase(), UserContextProvider.getCorrelationId());
//    headerMap.put("permissions", commaSeparatedPermissionsString(UserContextProvider.getPermissions()));
		if (Objects.nonNull(userHeaderOptions)) {
			headerMap.putAll(userHeaderOptions);
		}

		return headerMap;
	}

//  static String commaSeparatedPermissionsString(Set<TokenPermission> tokenPermissions) {
//    return TCollectionUtils
//        .nullSafeCollection(tokenPermissions).stream().map(Enum::name).collect(Collectors.joining(","));
//  }

	public static String fetchUserGlobalChannel(){
		String tenantId = UserContextProvider.getCurrentTenantId();
		if(System.getenv("CLUSTER_TYPE").equals("stage_cloud")){
			tenantId = "stg-"+tenantId;
		}
		return  tenantId
				+ "_"
				+ UserContextProvider.getCurrentDealerId()
				+ "_"
				+ "USER_"
				+ UserContextProvider.getCurrentUserId();
	}

	public static boolean isZeroOrNull(BigDecimal number) {
		if (Objects.isNull(number) || number.compareTo(BigDecimal.ZERO) == 0) {
			return true;
		}
		return false;
	}




	/**
	 *  Returns a deeply cloned java bean.
	 *
	 * @param fromBean java bean to be cloned.
	 * @return a new java bean cloned from fromBean.
	 */
	public static <T> T deepCopy(Object fromBean, Class<T> clazz) {
		if(Objects.isNull(fromBean)) {
			return null;
		}
		String jsonString = JsonUtil.toJson(fromBean);
		return  JsonUtil.fromJson(jsonString, clazz).orElse(null);
	}

	public static BigDecimal nullSafeBigDecimal(Money money){
		if(money==null){
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(money.getAmount()).divide(BigDecimal.valueOf(100)).setScale(2);
	}

	public static Long nullSafeLong(Long longValue){
		if(longValue==null){
			return 0l;
		}
		return longValue;
	}

	public boolean isValidNode(JsonNode jsonNode) {
		if(jsonNode != null && jsonNode.isValueNode())
			return true;
		return false;
	}

	public boolean isNotBlank(JsonNode jsonNode) {
		if(isValidNode(jsonNode) && TStringUtils.isNotBlank(jsonNode.textValue())) {
			return true;
		}
		return false;
	}

	public <E extends Enum<E>> boolean presentInEnum(String value, Class<E> enumClass) {
		for (E e : enumClass.getEnumConstants()) {
			if(e.name().equals(value)) { return true; }
		}
		return false;
	}

	public String excludeSpecialCharacters(String input, String delimiter){
		input = TStringUtils.nullSafeString(input);
		input = input.trim();
		return input.replaceAll("[^a-zA-Z0-9]", TStringUtils.nullSafeString(delimiter));
	}

	public boolean isSpecialCharacterPresent(String input){
		if(alphanumericPattern.matcher(TStringUtils.nullSafeString(input)).matches()){
			return false;
		}
		return true;
	}

	public boolean isProdEnv(){
		String clusterType = TStringUtils.nullSafeString(System.getenv("CLUSTER_TYPE")).toLowerCase();
		return clusterType.contains("prod");
	}

	public static int getAbsDiff(int value1, int value2) {
		return Math.abs((value1 - value2));
	}

	public static boolean isLocalClusterType(){
		return "local".equals(System.getenv(TekionCommonProperties.CLUSTER_TYPE));
	}

	public static String removeSpecialCharsAndWhiteSpaces(String bankName) {
		return bankName.replaceAll("[^a-zA-Z0-9]","").toUpperCase();
	}

	public static void setEmptyValueForAllStringFields(Object object) {
		Class<?> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			try {
				if (!Modifier.isStatic(field.getModifiers()) && String.class.equals(field.getType())) {
					field.setAccessible(true);
					field.set(object, "");
				}
			} catch (IllegalAccessException e) {
				log.error("Error occurred while initializing fields");
			}
		}
	}

	public static String getFullNameFromFNameAndLName(String fName, String lName, String placeholder) {
		if (TStringUtils.isNotBlank(fName) && TStringUtils.isNotBlank(lName)) {
			return fName + placeholder + lName;
		} else if (TStringUtils.isNotBlank(fName)) {
			return fName;
		} else if (TStringUtils.isNotBlank(lName)) {
			return lName;
		} else {
			return TConstants.BLANK_STRING;
		}
	}

	public static boolean checkEqualityForGivenString(String value, String constantInLowerCase) {
		return constantInLowerCase.equals(value.toLowerCase().trim());
	}


	public static String getCombinedValuesBySeparator(Collection<String> values, String separator) {
		if(TCollectionUtils.isEmpty(values)){
			return Strings.EMPTY;
		}
		return TStringUtils.join(values, separator);
	}
}

