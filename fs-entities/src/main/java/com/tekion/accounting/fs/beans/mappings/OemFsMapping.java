package com.tekion.accounting.fs.beans.mappings;

import com.tekion.accounting.fs.beans.MigrationMetaDataForFsEntry;
import com.tekion.core.beans.TBaseMongoBean;
import com.tekion.core.utils.UserContextProvider;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndexes({
		@CompoundIndex(
				name = "idx_fsCellGroupCode",
				def = "{'fsCellGroupCode':1}"
		),
		@CompoundIndex(
				name = "idx_year_oem_fsCellGroupCode",
				def = "{'year':1, 'oem': 1, 'fsCellGroupCode': 1}"
		),
		@CompoundIndex(
				name = "idx_fsId_fsCellGroupCode",
				def = "{'fsId':1, 'fsCellGroupCode': 1}"
		)


})
public class OemFsMapping extends TBaseMongoBean {

	public static final String GL_ACCT_ID = "glAccountId";
	public static final String FS_CELL_GROUP_CODE = "fsCellGroupCode";
	public static final String OEM_ID = "oemId";
	public static final String VERSION = "version";
	public static final String Year = "year";

	private String oemId;
	private Integer year;
	private Integer version;
	private String fsId;
	private String glAccountId;
	private String glAccountDealerId;
	private String fsCellGroupCode;

	private MigrationMetaDataForFsEntry migrationMetaDataForFsEntry;

	private String dealerId;
	private String siteId;
	private String createdByUserId;
	private String modifiedByUserId;

	private boolean migrated;
	private Long migratedTime;

	public String getUniversalId() {
		return "" + oemId + year + version + glAccountId + fsCellGroupCode + dealerId + siteId + fsId;
	}

	public OemFsMappingSnapshot toSnapshot(){
		OemFsMappingSnapshot snapshot = new OemFsMappingSnapshot();
		snapshot.setFsCellGroupCode(fsCellGroupCode);
		snapshot.setGlAccountId(glAccountId);
		snapshot.setOemId(oemId);
		snapshot.setDealerId(UserContextProvider.getCurrentDealerId());
		snapshot.setSiteId(siteId);
		snapshot.setCreatedTime(System.currentTimeMillis());
		snapshot.setModifiedTime(System.currentTimeMillis());
		snapshot.setCreatedByUserId(UserContextProvider.getCurrentUserId());
		return snapshot;
	}
}
