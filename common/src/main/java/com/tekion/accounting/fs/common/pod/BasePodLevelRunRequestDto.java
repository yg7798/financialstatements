package com.tekion.accounting.fs.common.pod;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * I can now specify any kind of payload.
 * examples supported
 *
 * runOnAbcDealers
 * runOnAllExceptAbcDealersExcludeQADealer
 * runForAllQAOnlyDealers
 */


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasePodLevelRunRequestDto {
	private List<DealerInfo> dealersToRunFor;
	private List<DealerInfo> runExceptFollowingDealers;

	//add this functionality. i dont wanna type and always pass QA dealership list.
	private Boolean excludeQADealershipsOnRun;
	private Boolean onlyQADealershipsOnRun;
}
