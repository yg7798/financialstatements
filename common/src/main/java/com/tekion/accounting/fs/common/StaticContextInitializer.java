package com.tekion.accounting.fs.common;

import com.tekion.accounting.fs.common.dpProvider.DpProvider;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.core.validation.TValidator;
import com.tekion.podclient.PodConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StaticContextInitializer {

	@Autowired
	private DpProvider dpProvider;

//	@Autowired
//	private PDFPrintService pdfPrintService;


	@Qualifier(value = AsyncContextDecorator.ASYNC_THREAD_POOL)
	@Autowired
	private TaskExecutor executor;

	@Autowired
	private PodConfigService podConfigService;

	@Autowired
	private GlobalService globalService;

	@Autowired
	private TValidator validator;

	@Autowired
	private DealerConfig dealerConfig;

	@PostConstruct
	public void init() {
		DpUtils.autowireStaticElem(dpProvider);
	}

}
