package com.tekion.accounting.fs.service.fsMapping;

import java.util.List;
import java.util.Set;

public interface FsMappingService {

    Set<String> deleteDuplicateMappings(List<String> fsIds);

    Set<String> deleteInvalidMappings(String fsId);
}
