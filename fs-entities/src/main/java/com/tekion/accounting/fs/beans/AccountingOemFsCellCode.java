package com.tekion.accounting.fs.beans;


import com.google.common.collect.Lists;
import com.tekion.accounting.fs.enums.FsCellCodeSource;
import com.tekion.accounting.fs.enums.OemValueType;
import com.tekion.core.beans.TBaseMongoBean;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CompoundIndexes({
		@CompoundIndex(
				name = "idx_code_year_oemId_country",
				def = "{'code':1, 'year':1, 'oemId':1, 'country':1}", unique = true
		),
		@CompoundIndex(
				name = "idx_year_oemId_country",
				def = "{'year':1, 'oemId':1, 'country':1}"
		)
})

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountingOemFsCellCode extends TBaseMongoBean {

	public static final String OEM_ID = "oemId";
	public static final String CODE = "code";
	public static final String YEAR = "year";
	public static final String VERSION = "version";
	public static final String DERIVED = "derived";


	public static final String additionInfoField_month = "month";
	public static final String additionInfoField_codeIdentifier = "codeIdentifier";
	public static final String OEM_CODE = "oemCode";
	public static final String DURATION_TYPE = "durationType";
	public static final String OEM_CODE_SIGN = "oemCodeSign";

	/**
	 * will be saved in `tags` and used for analytics
	 * */
	public static final String ACCOUNT_TYPE = "accountType";
	public static final String ACCOUNT_SUB_TYPE = "accountSubType";
	public static final String CATEGORY = "category";
	public static final String FS_GROUP = "fsGroup";
	public static final String FS_SUB_GROUP = "fsSubGroup";

	private String oemId;
	private String displayName;
	private String code;
	private Integer year;
	private Integer version;
	private String country;
	private boolean derived;
	private String subType;
	private String valueType;
	private String durationType;
	private String expression;
	private String oemDescription;
	private String groupCode;
	private String oemCode; // TBIN string
	private FsCellCodeSource source;

	/**
	 * @see OemValueType
	 * */

	private String oemValueType;

	private List<String> dependentFsCellCodes = Lists.newArrayList();
	private Map<String, String> additionalInfo = new HashMap<>();
	private Map<String, String> tags = new HashMap<>();
}
