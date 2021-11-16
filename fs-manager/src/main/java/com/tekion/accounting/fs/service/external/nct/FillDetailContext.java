package com.tekion.accounting.fs.service.external.nct;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.integration.Detail;
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