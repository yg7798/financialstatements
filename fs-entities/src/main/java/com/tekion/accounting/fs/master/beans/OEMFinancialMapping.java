package com.tekion.accounting.fs.master.beans;


import com.tekion.core.beans.TBaseMongoBean;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndex( name = "idx_fsId_glAccountId", def = "{'fsId':1, 'glAccountId':1}", unique = true)
public class OEMFinancialMapping extends TBaseMongoBean {
	public static final String YEAR = "year";
	public static final String OEM_ID = "oemId";
	public static final String GL_ACCOUNT_ID = "glAccountId";

	private String year;
	private String oemId;
	private String fsId;
	private String glAccountId;
	private String glAccountDealerId;
	private String oemAccountNumber;
	private String dealerId;
	private String siteId;
}
