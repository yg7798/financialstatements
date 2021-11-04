package com.tekion.accounting.fs.dto.oemPayload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialReportRequestBody {

	String year;
	String month;
}
