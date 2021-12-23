package com.tekion.accounting.fs.beans.common;

import com.tekion.accounting.fs.enums.FSType;
import com.tekion.core.beans.TBaseMongoBean;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
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
public class FSEntry extends TBaseMongoBean implements Cloneable{

	public static final String OEM_ID = "oemId";
	public static final String YEAR = "year";
	public static final String VERSION = "version";
	public static final String FS_TYPE = "fsType";
	public static final String FS_ID = "fsId";
	public static final int NAME_MAX_LENGTH = 64;

	private String oemId;
	private Integer year;
	private Integer version;
	private String siteId;
	@NotEmpty
	private String fsType;
	private String name;
	/*
	 * this is used for dealer ids to consider for consolidated FS
	 */
	private List<String> dealerIds;
	private String dealerId;
	private String createdByUserId;
	private String modifiedByUserId;
	private String parentFsEntryRef;

	private MigrationMetaDataForFsEntry migrationMetaDataForFsEntry;

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}


	public void updateNameToDefault(){
		if(TStringUtils.isNotBlank(getFsType())){
			this.name = oemId + "-" + FSType.valueOf(getFsType()).getDisplayName();
		}
	}

	public void updateNameIfEmpty(){
		String name = this.getName();
		if(TStringUtils.isBlank(name)){
			updateNameToDefault();
			return;
		}
		name = name.trim();
		if (name.length() > FSEntry.NAME_MAX_LENGTH) {
			name = name.substring(0, FSEntry.NAME_MAX_LENGTH);
		}
		this.setName(name);
	}

	public static void updateInfoForClonedFsEntry(FSEntry fsEntry){
		fsEntry.setId(new ObjectId().toHexString());
		fsEntry.setCreatedTime(System.currentTimeMillis());
		fsEntry.setModifiedTime(System.currentTimeMillis());
		fsEntry.setCreatedByUserId(UserContextProvider.getCurrentUserId());
		fsEntry.setModifiedByUserId(UserContextProvider.getCurrentUserId());
	}
}
