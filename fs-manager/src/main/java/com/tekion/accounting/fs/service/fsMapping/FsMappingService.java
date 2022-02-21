package com.tekion.accounting.fs.service.fsMapping;

import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.dto.mappings.OemFsMappingUpdateDto;


import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FsMappingService {

    List<OemFsMapping> getOemFsMapping(String fsId);

    List<OemFsMapping> updateOemFsMapping(OemFsMappingUpdateDto requestDto);

    Set<String> deleteDuplicateMappings(List<String> fsIds);

    Set<String> deleteInvalidMappings(String fsId);

    List<OemFsMapping> getMappingsByGLAccounts(String fsId, List<String> glAccounts);

    List<OemFsMapping> copyFsMappings(String fromFsId, String toFsId);

    void migrateFsMappingsFromYearToYear(Integer fromYear, Integer toYear, List<String> oemIds);

    List<OemFsMapping> getFsMappingsByOemIdAndGroupCodes(Integer year, List<String> groupCodes, List<String> oemIds, boolean ignoreFsType);

	void hardDeleteMappings(String fsId);

    void deleteMappingsByGroupCodes(List<String> groupDisplayNames, String oemId, Integer year, String country);

    void replaceGroupCodesInMappings(Map<String, String> groupDisplayNames, String oemId, Integer year, String country);
}
