package com.tekion.accounting.fs.service.fsMetaData;

import com.tekion.accounting.fs.beans.OemFSMetadataCellsInfo;
import com.tekion.accounting.fs.dto.OemFSMetadataCellMappingCreateDto;
import com.tekion.accounting.fs.dto.OemFsMetadataCellMappingInfo;

public interface OemFSMetadataMappingService {

	OemFSMetadataCellsInfo getOemFsMetadataCellMappings(String oemId, Integer year, String version);

	void saveOemFsMetadataCellMappings(OemFSMetadataCellMappingCreateDto reqDto);

	void deleteOemFsMetadataCellMappings(String oemId, Integer year, String version, String country);

	void updateOemFsMetadataCellMappings(OemFsMetadataCellMappingInfo oemFsMetadataCellMappingInfo);
}
