package com.tekion.accounting.fs.dto.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FSSubmitResponse {
	private IntegrationResponse response;
	private String status;

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Data
	public static class IntegrationResponse{
		private String status;
		@JsonProperty(value = "errorDetail")
		private List<ErrorDetail> errorDetail;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ErrorDetail{
		private String description;
		private String code;
		private String reason;
	}
}
