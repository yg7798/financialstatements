package com.tekion.accounting.fs.client;

import com.tekion.core.feign.TFeignErrorUtils;
import com.tekion.core.feign.exceptions.TResponseException;
import feign.FeignException;

public class FSClientException extends TResponseException {
	public FSClientException(FeignException exception, TFeignErrorUtils.ErrorDetails details) {
		super(exception, details);
	}

	public FSClientException(FeignException exception) {
		super(exception, new TFeignErrorUtils.ErrorDetails());
	}
}
