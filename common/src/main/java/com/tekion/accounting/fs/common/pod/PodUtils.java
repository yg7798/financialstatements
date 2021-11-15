package com.tekion.accounting.fs.common.pod;

import com.google.common.collect.Lists;
import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.common.core.dealerInfo.DealerInfo;
import com.tekion.client.globalsettings.beans.TenantInfo;
import com.tekion.client.globalsettings.beans.dto.DealerInfoWithOEMDetails;
import com.tekion.core.exceptions.TConfigurationNotFoundException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

import static com.tekion.core.excelGeneration.models.utils.TCollectionUtils.isNotEmpty;
import static java.lang.System.getenv;

@UtilityClass
public class PodUtils {
	private static GlobalService globalService;
	public static final String CLUSTER_TYPE = getClusterType();

	public static void initialiseServices(GlobalService globalService1){
		globalService = globalService1;
	}


	public static List<DealerInfo> extractDealerInfoDetailsToRunFor(BasePodLevelRunRequestDto podLevelRunRequestDto) {
		// validating again because devs dont use correctly.
		validateRequest(podLevelRunRequestDto);
		List<DealerInfo> allActiveDealerTenantList = getAllDealerInfoDetailsInPod();
		return applyFiltersAndGetDealerInfoMatchingRequest(allActiveDealerTenantList,podLevelRunRequestDto);
	}

	private static void validateRequest(BasePodLevelRunRequestDto podLevelRunRequestDto) {
	}

	private static List<DealerInfo> applyFiltersAndGetDealerInfoMatchingRequest(List<DealerInfo> allActiveDealerTenantList, BasePodLevelRunRequestDto podLevelRunRequestDto) {
		Validator.validateRequest(podLevelRunRequestDto);
		List<DealerInfo> dealerInfoList  = Lists.newArrayList();
		for (DealerInfo dealerInfo : allActiveDealerTenantList) {
			if(checkIfThisDealerShouldBeSelected(dealerInfo,podLevelRunRequestDto)){
				dealerInfoList.add(dealerInfo);
			}
		}
		return dealerInfoList;
	}

	private static boolean checkIfThisDealerShouldBeSelected(DealerInfo dealerInfo, BasePodLevelRunRequestDto podLevelRunRequestDto) {
		// if object not there, then run for all
		if(Objects.isNull(podLevelRunRequestDto)){
			return true;
		}
		// as validator ensure only of these 4 things 4 flag/list is not empty or true, this check will work
		// create more complicated ones in future?
		// TODO: 09/06/21 want functionality to run on allNonQADealers except abc. no use case as of now. but good to have. need to code this.

		DealerInfo dealerInfoWithoutOemDetails = getDealerInfoCopy(dealerInfo);
		if(isBoolSetToTrue(podLevelRunRequestDto.getExcludeQADealershipsOnRun())){
			return !isDealerInternalQADealerOnProd(dealerInfo.getDealerId(),dealerInfo.getTenantId());
		}
		if(isBoolSetToTrue(podLevelRunRequestDto.getOnlyQADealershipsOnRun())){
			return isDealerInternalQADealerOnProd(dealerInfo.getDealerId(),dealerInfo.getTenantId());
		}
		if(isNotEmpty(podLevelRunRequestDto.getDealersToRunFor())){
			return podLevelRunRequestDto.getDealersToRunFor().contains(dealerInfoWithoutOemDetails);
		}
		if(isNotEmpty(podLevelRunRequestDto.getRunExceptFollowingDealers())){
			return !podLevelRunRequestDto.getRunExceptFollowingDealers().contains(dealerInfoWithoutOemDetails);
		}
		return true;
	}

	public static boolean isBoolSetToTrue(Boolean bool) {
		return Objects.nonNull(bool) && bool;
	}

	private static List<DealerInfo> getAllDealerInfoDetailsInPod() {
		List<DealerInfo> toReturn = Lists.newArrayList();

		List<TenantInfo> tenantInfos = globalService.fetchActiveTenants();
		for (TenantInfo tenantInfo : tenantInfos) {
			List<DealerInfoWithOEMDetails> dealerInfoWithOEMDetails = globalService.fetchAllDealersForATenant(tenantInfo.getTenantId());
			for (DealerInfoWithOEMDetails dealerInfoWithOEMDetail : dealerInfoWithOEMDetails) {
				DealerInfo dealerInfo = new DealerInfo();
				dealerInfo.setDealerId(dealerInfoWithOEMDetail.getDealerId());
				dealerInfo.setTenantId(dealerInfoWithOEMDetail.getTenantId());
				toReturn.add(dealerInfo);
			}
		}
		return toReturn;
	}

	private static DealerInfo getDealerInfoCopy(DealerInfo dealerInfo ){
		if(Objects.isNull(dealerInfo)){
			return null;
		}
		DealerInfo dealerInfoToReturn = new DealerInfo();
		dealerInfoToReturn.setTenantId(dealerInfo.getTenantId());
		dealerInfoToReturn.setDealerId(dealerInfo.getDealerId());
		return dealerInfoToReturn;
	}

	public static boolean isDealerInternalQADealerOnProd(String dealerId, String tenantId){
		String clusterType = CLUSTER_TYPE;

		if(clusterType.startsWith("prod")) {

			if (("4".equalsIgnoreCase(dealerId) && "techmotors".equalsIgnoreCase(tenantId))
					||
					"trainingsandbox".equalsIgnoreCase(tenantId)
					||
					"tekiondemos".equalsIgnoreCase(tenantId)
			) {
				return true;
			} else {
				return false;
			}
		}
		// dont have a list of QA dealerships as of now for other envs.
		return false;
	}

	private static String getClusterType() {
		String clusterType = getenv("CLUSTER_TYPE");
		if (StringUtils.isBlank(clusterType))
			throw new TConfigurationNotFoundException("CLUSTER_TYPE IS NOT SET");
		return clusterType;
	}

}

