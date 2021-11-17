package com.tekion.accounting.fs.beans.accountingInfo;

import lombok.Data;

import java.util.Map;

@Data
public class FSPreferences {
	private Boolean useSnapshotsForMonthlyCells;
	private Map<String, Boolean> roundOffGlAccountBalances;
}
