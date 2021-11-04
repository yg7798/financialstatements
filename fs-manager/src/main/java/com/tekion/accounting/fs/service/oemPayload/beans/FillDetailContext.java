package com.tekion.accounting.fs.service.oemPayload.beans;

import com.tekion.accounting.fs.beans.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.OemConfig;
import com.tekion.accounting.fs.beans.integration.Detail;
import com.tekion.accounting.fs.dto.cellcode.FsCodeDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FillDetailContext {
	private BigDecimal value;
	private Detail detail;
	private OemConfig oemConfig;
	private FsCodeDetail cellDetail;
	private AccountingOemFsCellCode cellCode;
	private String valueString;
}