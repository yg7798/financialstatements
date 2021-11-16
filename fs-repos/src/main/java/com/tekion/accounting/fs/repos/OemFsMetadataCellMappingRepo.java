package com.tekion.accounting.fs.repos;

import com.tekion.accounting.fs.beans.common.OemFSMetadataCellsInfo;

import java.util.List;

public interface OemFsMetadataCellMappingRepo {

    OemFSMetadataCellsInfo getOemFsMetadataCellMapping(String oemId, Integer year, String version, String country);

    List<OemFSMetadataCellsInfo> getOemFsMetadataCellMappingForAllYears(String oemId, String country);

    void insertBulk(List<OemFSMetadataCellsInfo> beans);

    void delete(String oemId, Integer year, String version, String country);

    void update(OemFSMetadataCellsInfo updateDto);

    void addCountryInOemFsMetaDataMappings();
}
