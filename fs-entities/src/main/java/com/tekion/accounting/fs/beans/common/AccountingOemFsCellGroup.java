package com.tekion.accounting.fs.beans.common;

import com.tekion.accounting.fs.enums.FsCellCodeSource;
import com.tekion.core.beans.TBaseMongoBean;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@CompoundIndexes({
		@CompoundIndex(
				name = "idx_group_cdkPcl_year_oemId_country",
				def = "{'groupCode':1, 'cdkPcl':1, 'year':1, 'oemId':1, 'country':1}", unique = true
		),
		@CompoundIndex(
				name = "idx_country_year_oemId_group",
				def = "{'country':1, 'year':1, 'oemId':1, 'groupCode': 1}"
		)
})

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountingOemFsCellGroup extends TBaseMongoBean {

	public static final String OEM_ID = "oemId";
	public static final String YEAR = "year";
	public static final String VERSION = "version";

	public static final String CDK_PCL = "cdkPcl";
	public static final String DB_PCL = "dbPcl";
	public static final String OEM_ACCT_NO = "oemAccountNumber";
	public static final String AUTOMATE_PCL_CODE = "automatePcl";
	public static final String GROUP_DISPLAY_NAME = "groupDisplayName";
	public static final String GROUP_CODE = "groupCode";


	private String oemId;
	private Integer year;
	private Integer version;
	private String country;

	private String groupDisplayName;
	private String groupCode;
	private String cdkPcl;
	private String dbPcl;
	private String rrPcl;
	private String dominionPcl;
	private String quorumPcl;
	private String autosoftPcl;
	private String automatePcl;
	private String pbsPcl;
	private String oemAccountNumber;
	private String dealerTrackPcl;
	private FsCellCodeSource source;

	public void updateGroupCodes(AccountingOemFsCellGroup toUpdate) {
		this.setAutomatePcl(toUpdate.getAutomatePcl());
		this.setAutosoftPcl(toUpdate.getAutosoftPcl());
		this.setCdkPcl(toUpdate.getCdkPcl());
		this.setDbPcl(toUpdate.getDbPcl());
		this.setDealerTrackPcl(toUpdate.getDealerTrackPcl());
		this.setDominionPcl(toUpdate.getDominionPcl());
		this.setPbsPcl(toUpdate.getPbsPcl());
		this.setQuorumPcl(toUpdate.getQuorumPcl());
		this.setRrPcl(toUpdate.getRrPcl());
		this.setModifiedTime(System.currentTimeMillis());
	}
}
