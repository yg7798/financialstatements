package com.tekion.accounting.fs.service.fsMapping;

import com.tekion.accounting.fs.beans.mappings.OemFsMapping;

import java.util.List;
import java.util.Set;

public interface FsMappingService {

    Set<String> deleteDuplicateMappings(List<String> fsIds);

    Set<String> deleteInvalidMappings(String fsId);

    List<OemFsMapping> getMappingsByGLAccounts(String fsId, List<String> glAccounts);

    List<OemFsMapping>  getOemFsMapping(String fsId);

    List<OemFsMapping> copyFsMappings(String fromFsId, String toFsId);

    void migrateFsMappingsFromYearToYear(Integer fromYear, Integer toYear, List<String> oemIds);

    List<OemFsMapping> getFsMappingsByOemIdAndGroupCodes(Integer year, List<String> groupCodes, List<String> oemIds);

	void hardDeleteMappings(String fsId);

}
