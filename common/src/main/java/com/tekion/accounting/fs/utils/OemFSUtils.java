package com.tekion.accounting.fs.utils;


import com.google.common.collect.Lists;
import com.tekion.accounting.fs.TConstants;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TStringUtils;
import org.slf4j.Logger;

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



}

