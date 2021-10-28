package com.tekion.accounting.fs;

import com.tekion.accounting.fs.utils.MDCUtils;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class AsyncContextDecorator implements TaskDecorator {
	public static final String ASYNC_THREAD_POOL = "asyncExecutor";

	@Override
	public Runnable decorate(Runnable runnable) {
		UserContext userContext = UserContextProvider.getContext();
		Map<String,String> mdcContext = MDC.getCopyOfContextMap();
		return () -> {
			try {
				UserContextProvider.setContext(userContext);
				MDCUtils.setMDCParamsFromUserContext(mdcContext);
				runnable.run();
			} finally {
				UserContextProvider.unsetContext();
				MDC.clear();
			}
		};
	}
}