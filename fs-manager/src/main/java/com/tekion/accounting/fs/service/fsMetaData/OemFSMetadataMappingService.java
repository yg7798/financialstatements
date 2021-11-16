package com.tekion.accounting.fs.service.fsMetaData;

import com.tekion.accounting.fs.beans.common.OemFSMetadataCellsInfo;

public interface OemFSMetadataMappingService {

	OemFSMetadataCellsInfo getOemFsMetadataCellMappings(String oemId, Integer year, String version);

	void saveOemFsMetadataCellMappings(OemFSMetadataCellMappingCreateDto reqDto);

	void deleteOemFsMetadataCellMappings(String oemId, Integer year, String version, String country);

	void updateOemFsMetadataCellMappings(OemFsMetadataCellMappingInfo oemFsMetadataCellMappingInfo);
}
