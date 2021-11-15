package com.tekion.accounting.fs.beans.common;

import com.tekion.core.beans.TBaseMongoBean;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@CompoundIndexes({
		@CompoundIndex(
				name = "idx_oem_year_version_site",
				def = "{'year':1, 'version':1, 'oemId':1,'siteId':1}"
		)
})
@Document(value = "oemFsMappingInfo")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FSEntry extends TBaseMongoBean {

	public static final String OEM_ID = "oemId";
	public static final String YEAR = "year";
	public static final String VERSION = "version";
	public static final String FS_TYPE = "fsType";
	public static final String FS_ID = "fsId";

	private String oemId;
	private Integer year;
	private Integer version;
	private String siteId;
	private String fsType;
	/*
	 * this is used for dealer ids to consider for consolidated FS
	 */
	private List<String> dealerIds;

	private String dealerId;
	private String createdByUserId;
	private String modifiedByUserId;

	private MigrationMetaDataForFsEntry migrationMetaDataForFsEntry;

}
