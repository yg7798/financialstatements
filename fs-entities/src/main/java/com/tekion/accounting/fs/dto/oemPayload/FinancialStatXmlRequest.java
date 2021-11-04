package com.tekion.accounting.fs.dto.oemPayload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinancialStatXmlRequest {
	private String tenantName;
	private String dealerId;
	private String dealerNumber;
	private String year;
	private String month;
}
