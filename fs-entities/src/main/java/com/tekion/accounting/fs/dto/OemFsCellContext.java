package com.tekion.accounting.fs.dto;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.OEMFsCellCodeSnapshot;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.memo.HCWorksheet;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.dto.cellcode.FsCodeDetail;
import com.tekion.as.models.beans.TrialBalanceRow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OemFsCellContext {

	private Integer requestedYear;
	private Integer requestedMonth;
	private String defaultPrecision;
	private Long fsTime;
	private boolean roundOff;
	@Builder.Default private AccountingOemFsCellCode fsCellCode = new AccountingOemFsCellCode();
	@Builder.Default private AccountingOemFsCellCode parentFsCellCode = new AccountingOemFsCellCode();
	@Builder.Default private Map<String, List<String>> groupCodeVsGlAccountsMap = Maps.newHashMap();
	@Builder.Default private Map<String, AccountingOemFsCellCode> codeVsCellInfoMap = Maps.newHashMap();
	@Builder.Default private Map<String, BigDecimal> codeVsValueMap = Maps.newHashMap();
	@Builder.Default private Map<String, FsCodeDetail> codeVsDetailsMap = Maps.newHashMap();
	@Builder.Default private Map<String, TrialBalanceRow> trialBalanceRowMap = Maps.newHashMap();
	@Builder.Default private Map<String, String> codeVsExpressionMap = Maps.newHashMap();
	@Builder.Default private Map<String, String> codeIdentifierVsExpressionMap = Maps.newHashMap();
	@Builder.Default private Map<String, MemoWorksheet> memoKeyToValueMap = Maps.newHashMap();
	@Builder.Default private Map<String, HCWorksheet> hcKeyValueMap = Maps.newHashMap();
	@Builder.Default private List<AccountingOemFsCellCode> fsCellCodes = new ArrayList<>();
	@Builder.Default private List<AccountingOemFsCellCode> derivedFSCellCodes = new ArrayList<>();
	@Builder.Default private List<AccountingOemFsCellCode> monthlyFSCellCodes = new ArrayList<>();
	@Builder.Default private List<AccountingOemFsCellCode> nonDerivedFSCellCodes = new ArrayList<>();
	@Builder.Default private Map<Integer, Map<String, Map<String, BigDecimal>>> glBalCntInfoForFS = Maps.newHashMap();
	@Builder.Default private Map<Integer, Map<String, OEMFsCellCodeSnapshot>> monthToCellCodeSnapshots = Maps.newHashMap();
	@Builder.Default private Set<String> monthlyCodes = new HashSet<>();
	private AccountingInfo accountingInfo;
	private OemConfig oemConfig;
}
