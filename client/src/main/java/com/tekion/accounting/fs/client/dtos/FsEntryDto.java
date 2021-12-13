package com.tekion.accounting.fs.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FsEntryDto {
	private String id;
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
}
