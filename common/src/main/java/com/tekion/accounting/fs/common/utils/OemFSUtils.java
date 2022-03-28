package com.tekion.accounting.fs.common.utils;


import com.google.common.collect.Lists;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TStringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import parsii.eval.Parser;
import parsii.tokenizer.ParseException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OemFSUtils {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(OemFSUtils.class);
	private static Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
	private static final Map<String,String> groupCodeCharReplacementMetaData = new HashMap<String, String>(){
		{
			put("+", "plus");
			put("-", "_");
			put("&", "n");
			put("<", "lt");
			put(">", "gt");
		}
	};

	public static List<String> getCodesFromExpression(String expression){
		List<String> codes = Lists.newArrayList();
		Matcher m = pattern.matcher(TStringUtils.nullSafeString(expression));
		while(m.find()) {
			codes.add(m.group(1));
		}
		return codes;
	}


	public static String createGroupCode(String groupDisplayName) {
		String groupCode = "_" + groupDisplayName; // prepend underscore
		for (Map.Entry<String, String> e : groupCodeCharReplacementMetaData.entrySet()) {
			groupCode = groupCode.replace(e.getKey(),e.getValue());
		}
		if(!Pattern.matches(TConstants.REGEX_ALPHA_NUMERIC_AND_UNDERSCORE, groupCode)){
			log.error("groupCode : {} ", groupCode);
			throw new TBaseRuntimeException();
		}

		return groupCode;
	}

	public static String getExpressionReplacedByValues(String str, Map<String, BigDecimal> map){
		StringSubstitutor sub = new StringSubstitutor(map);
		return sub.replace(str);
	}

	public static BigDecimal getCalculatedAmount(String expression) {
		if(TStringUtils.isBlank(expression)){
			return BigDecimal.ZERO;
		}

//        log.info("expression : {} " , expression );
		try {
			return BigDecimal.valueOf(Parser.parse(expression).evaluate()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		} catch (NumberFormatException e){
//            log.warn("Exception while calculation {} {}",expression, e.getMessage());
			return BigDecimal.ZERO;
		} catch (ParseException e) {
			log.error("ParseException while calculation {} {}",expression, e.getMessage());
		}
		return BigDecimal.ZERO;
	}

	public static String createFsCellCode(String groupCode, String valueType, String durationType) {
		return groupCode + "_" + valueType + "_" + durationType;
	}

	public static String getExpressionReplacedByExpression(String expression, Map<String, String> codeVsExpressionMap) {
		StringSubstitutor sub = new StringSubstitutor(codeVsExpressionMap);
		return sub.replace(expression);
	}
}

