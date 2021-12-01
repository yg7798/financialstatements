package com.tekion.accounting.fs.dto.fsEntry;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.common.MigrationMetaDataForFsEntry;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FsEntryCreateDto {


	@NotNull
	private OEM oemId;
	@NotNull private Integer year;
	@NotNull private Integer version;
	private String siteId;
	@NotNull private FSType fsType;
	private MigrationMetaDataForFsEntry migrationMetaDataForFsEntry;
	@NotNull List<String> dealerIds = new ArrayList<>();

	public FSEntry createFSEntry() {
		if(TStringUtils.isBlank(siteId)){
			siteId = UserContextUtils.getSiteIdFromUserContext();
		}
		FSEntry fsEntry =  FSEntry.builder()
				.oemId(oemId.name())
				.year(year)
				.version(version)
				.dealerId(UserContextProvider.getCurrentDealerId())
				.fsType(fsType.name())
				.dealerIds(dealerIds)
				.siteId(siteId)
				.migrationMetaDataForFsEntry(migrationMetaDataForFsEntry)
				.createdByUserId(UserContextProvider.getCurrentUserId())
				.modifiedByUserId(UserContextProvider.getCurrentUserId())
				.build();

		fsEntry.setCreatedTime(System.currentTimeMillis());
		fsEntry.setModifiedTime(System.currentTimeMillis());
		return fsEntry;
	}
}