package com.tekion.accounting.fs.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountingInfo {

	private static final String SALES_CODE = "salesCode";
	private static final String SALES_ZONE_CODE = "salesZoneCode";
	private static final String PNA_CODE = "pnaCode";
	private static final String YTD_TERM = "ytdTerm";
	private static final String MTD_TERM = "mtdTerm";

	private String id;
	private String dealerId;
	private String primaryOEM;
	private Set<String> supportedOEMs;
	private String createdByUserId;
	private String modifiedByUserId;
	private boolean bsdPresent; // Body shop department
	private boolean nodPresent; // New Other Dep
	private Boolean fsRoundOffOffset;
	private List<FsOemPayloadInfo> oemPayloadInfos;
	private String salesCode;
	private String salesZoneCode;
	private String pnaCode;
	private FSPreferences fsPreferences;
	/**
	 * oemId, memo key
	 * */
	private Map<String, String> oemToOffsetCellMap;

	@Data
	public static class FsOemPayloadInfo {
		private String oem;
		private DurationType durationType;
		private List<Detail> details;
	}

	@Data
	public static class FSPreferences {
		private Boolean useSnapshotsForMonthlyCells;
		private Map<String, Boolean> roundOffGlAccountBalances;
	}

	@Data
	public static class Detail {
		String accountId;
		String accountValue;
		String oemCodeSign;
		String description;
		String unit1;
		String unit2;
		String balance1;
		String balance2;
	}

	public enum DurationType {
		YTD,
		MTD
	}
}
