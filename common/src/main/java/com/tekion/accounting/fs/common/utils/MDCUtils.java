package com.tekion.accounting.fs.common.utils;

import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContext;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.Map;

@UtilityClass
public class MDCUtils {
	private static final String serviceName = System.getenv("service_name");
	public static final String TRACE_ID = "trace-id";

	public static void setMDCParamsFromUserContext(UserContext userContext){
		MDC.put("tId", userContext.getTenantId());
		MDC.put("dId", userContext.getDealerId());
		MDC.put("uId", userContext.getUserId());
//        MDC.put("corId", userContext.getC);
//        MDC.put("reqId", servletRequest.getHeader(REQUEST_ID_KEY));
	}

	public static void setMDCParamsFromUserContext(Map<String,String> mdcContext){
		for(String key : TCollectionUtils.nullSafeMap(mdcContext).keySet()){
			MDC.put(key, mdcContext.get(key));
		}
	}

	public static void clearMDC(){
		MDC.clear();
	}
}
