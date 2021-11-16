package com.tekion.accounting.fs.service.fsMetaData;

import com.tekion.accounting.fs.beans.common.OemFSMetadataCellsInfo;
import com.tekion.accounting.fs.repos.OemFsMetadataCellMappingRepo;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OemFsMetadataMappingServiceImpl implements OemFSMetadataMappingService{

	private final OemFsMetadataCellMappingRepo oemFsMetadataCellMappingRepo;
	private final DealerConfig dealerConfig;

	@Override
	public void saveOemFsMetadataCellMappings(OemFSMetadataCellMappingCreateDto reqDto) {
		List<OemFSMetadataCellsInfo> oemFSMetadataCellsInfoList = reqDto.convertToDealerInfoMappingList();
		oemFsMetadataCellMappingRepo.insertBulk(oemFSMetadataCellsInfoList);
	}

	@Override
	public void deleteOemFsMetadataCellMappings(String oemId, Integer year, String version, String country) {
		oemFsMetadataCellMappingRepo.delete(oemId, year, version, country);
	}

	@Override
	public void updateOemFsMetadataCellMappings(OemFsMetadataCellMappingInfo oemFsMetadataCellMappingInfo) {
		oemFsMetadataCellMappingRepo.update(oemFsMetadataCellMappingInfo.convertToDealerMappingInfo());
	}

	@Override
	public OemFSMetadataCellsInfo getOemFsMetadataCellMappings(String oemId, Integer year, String version) {
		return oemFsMetadataCellMappingRepo.getOemFsMetadataCellMapping(oemId, year, version, dealerConfig.getDealerCountryCode());
	}

}
