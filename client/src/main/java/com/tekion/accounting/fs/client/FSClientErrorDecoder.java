package com.tekion.accounting.fs.client;

import com.tekion.core.feign.TFeignErrorUtils;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

import static com.tekion.core.feign.TFeignErrorUtils.fromErrorResponse;
import static com.tekion.core.feign.TFeignErrorUtils.getErrorDetailsOrNull;
import static java.util.Objects.nonNull;


public class FSClientErrorDecoder implements ErrorDecoder {
	@Override
	public Exception decode(String methodKey, Response response) {
		FeignException exception = fromErrorResponse(methodKey, response);
		final TFeignErrorUtils.ErrorDetails errorDetails = getErrorDetailsOrNull(exception.content());
		if (nonNull(errorDetails)) {
			return new FSClientException(exception, errorDetails);
		} else {
			throw new FSClientException(exception);
		}
	}
}
