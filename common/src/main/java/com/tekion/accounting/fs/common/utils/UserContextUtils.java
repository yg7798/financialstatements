package com.tekion.accounting.fs.common.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.core.dealerInfo.DealerInfoDto;
import com.tekion.accounting.fs.common.core.dealerInfo.DealerInfo;
import com.tekion.accounting.fs.dto.mappings.OemSiteDetailsDto;
import com.tekion.admin.beans.dealersetting.DealerMaster;
import com.tekion.client.globalsettings.beans.Status;
import com.tekion.client.globalsettings.beans.TenantInfo;
import com.tekion.client.globalsettings.beans.dto.DealerInfoWithOEMDetails;
import com.tekion.core.utils.*;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

import static com.tekion.core.utils.TGlobalConstants.TEK_SITE_ID_PRESENT_KEY;

@UtilityClass
public class UserContextUtils {

	private static final boolean isStage = System.getenv("CLUSTER_TYPE").contains("stage");

	public static UserContext buildUserContext(String dealerId,String tenantId,String userId){
		return new UserContext(userId,tenantId,dealerId);
	}

	public static List<UserContext> createUserContextForAllDealers(Map<String, String> dealerIdToNameMapping, GlobalService globalService) {
		List<TenantInfo> tenantInfos = globalService.fetchActiveTenants();
		return getUCsForTenants(dealerIdToNameMapping, tenantInfos, globalService);
	}

	public static List<UserContext> getUserContextsForTenant(Map<String, String> dealerIdToNameMapping, GlobalService globalService){
		Map<String, TenantInfo> tenantInfoMap = TCollectionUtils.transformToMap(globalService.fetchActiveTenants(), TenantInfo::getTenantId);
		List<TenantInfo> tenantInfos = new ArrayList<>();
		tenantInfos.add(tenantInfoMap.get(UserContextProvider.getCurrentTenantId()));

		return getUCsForTenants(dealerIdToNameMapping, tenantInfos, globalService);
	}

	public static List<UserContext> getUCsForTenants(Map<String, String> dealerIdToNameMapping, List<TenantInfo> tenantInfos, GlobalService globalService){
		List<UserContext> userContextForAllDealers = Lists.newArrayList();

		for (TenantInfo tenantInfo : TCollectionUtils.nullSafeList(tenantInfos)) {
			for (DealerInfoWithOEMDetails dealerInfo : globalService.fetchAllDealersForATenant(tenantInfo.getTenantId())) {
				String tenantIdDealerId = dealerInfo.getTenantId() + "_" + dealerInfo.getDealerId();
				dealerIdToNameMapping.put(tenantIdDealerId, dealerInfo.getDisplayName());
				userContextForAllDealers.add(getContext(dealerInfo));

			}
		}
		return userContextForAllDealers;
	}
	public static List<UserContext> createUserContextForGivenTenantId(Map<String,String> dealerIdVsTenantIdMap,GlobalService globalService){
		List<UserContext> userContextForGivenTenant = Lists.newArrayList();
		Map<String, String> dealerIdToNameMapping = Maps.newHashMap();
		for (String tenantId : dealerIdVsTenantIdMap.values().stream().collect(Collectors.toSet())) {
			for (DealerInfoWithOEMDetails dealerInfo : globalService.fetchAllDealersForATenant(tenantId)) {
				if(dealerIdVsTenantIdMap.containsKey(dealerInfo.getDealerId())) {
					String tenantIdDealerId = dealerInfo.getTenantId() + "_" + dealerInfo.getDealerId();
					dealerIdToNameMapping.put(tenantIdDealerId, dealerInfo.getDisplayName());
					userContextForGivenTenant.add(getContext(dealerInfo));
				}
			}
		}
		return userContextForGivenTenant;
	}

	public static List<UserContext> createUserContextForPayload(DealerInfoDto dealerInfoDto, GlobalService globalService){
		List<UserContext> userContextForAllDealers=Lists.newArrayList();
		if(TCollectionUtils.isEmpty(dealerInfoDto.getDealerInfoList())){

			List<TenantInfo> tenantInfos = globalService.fetchActiveTenants();

			for (TenantInfo tenantInfo : TCollectionUtils.nullSafeList(tenantInfos)) {
				for (DealerInfoWithOEMDetails dealerInfo : globalService.fetchAllDealersForATenant(tenantInfo.getTenantId())) {
					if(filterOutDealer(dealerInfo,dealerInfoDto)){
						continue;
					}
					userContextForAllDealers.add(getContext(dealerInfo));

				}
			}
			return userContextForAllDealers;
		}
		else{
			Map<String, String> dealerIdVsTenantIdMap = Maps.newHashMap();
			for (DealerInfo dealerInfo : dealerInfoDto.getDealerInfoList()) {
				dealerIdVsTenantIdMap.put(dealerInfo.getDealerId(), dealerInfo.getTenantId());
			}
			userContextForAllDealers = createUserContextForGivenTenantId(dealerIdVsTenantIdMap, globalService);
			return userContextForAllDealers;

		}

	}

	private static boolean filterOutDealer(DealerInfoWithOEMDetails dealerInfo, DealerInfoDto dealerInfoDto) {
		if(!dealerInfoDto.isIncludeQaDealers() && dealerInfo.isInternal()){
			return true;
		}

		Status status = dealerInfo.getStatus();
		if(!dealerInfoDto.isIncludeDecommissionedDealers() && Objects.nonNull(status) && !status.equals(Status.live)){
			return true;
		}
		return false;
	}

	public static UserContext getContext(DealerInfoWithOEMDetails dealerInfoWithOEMDetails) {
		UserContext userContext = UserContextProvider.createCopy();
		userContext.setDealerId(dealerInfoWithOEMDetails.getDealerId());
		userContext.setTenantId(dealerInfoWithOEMDetails.getTenantId());
		userContext.setDseUserContext(DSEUserContext.builder().tenantName(dealerInfoWithOEMDetails.getTenantId()).dealerId(dealerInfoWithOEMDetails.getDealerId())
				.roleId("1").tekionApiToken("").build());
		return userContext;
	}

	public static String getTenantIdAndDealerIdAsString() {
		return UserContextProvider.getCurrentTenantId() + "_" +UserContextProvider.getCurrentDealerId();
	}

	public static UserContext getContextFromDealerAndTenant(String dealerId, String tenantId){

		DealerInfoWithOEMDetails dealerInfoWithOEMDetails = new DealerInfoWithOEMDetails();
		dealerInfoWithOEMDetails.setDealerId(dealerId);
		dealerInfoWithOEMDetails.setTenantId(tenantId);
		return getContext(dealerInfoWithOEMDetails);
	}

	public static String getDefaultSiteId(){
		return "-1_" + UserContextProvider.getCurrentDealerId();
	}

	public static String getSiteIdFromUserContext(){
		if(UserContextProvider.getForwardedHeaderMap().containsKey(TEK_SITE_ID_PRESENT_KEY) &&
				TConstants.TRUE.equals(UserContextProvider.getForwardedHeaderMap().get(TEK_SITE_ID_PRESENT_KEY).get(0))) {
			return UserContextProvider.getForwardedHeaderMap().get(TGlobalConstants.TEK_SITE_ID_KEY).get(0);
		}
		return getDefaultSiteId();
	}

	public static String getTenantId(String tenantId){
		if(isStage && TStringUtils.isNotBlank(tenantId) && !tenantId.startsWith("stg-")){
			return "stg-"+ tenantId;
		}
		return tenantId;
	}

	public static boolean areDealerIdsBelongsToSameTenant(List<String> dealerIds, String tenantId, GlobalService globalService){
		List<DealerInfoWithOEMDetails>  allDealersForATenant = globalService.fetchAllDealersForATenant(tenantId);
		List<String> dealerIdsBelongsToTenant = allDealersForATenant.stream().map(DealerInfoWithOEMDetails :: getDealerId).collect(Collectors.toList());
		return dealerIdsBelongsToTenant.containsAll(TCollectionUtils.nullSafeList(dealerIds));
	}

	public static UserContext buildUserContext(String dealerId){
		UserContext userContext=new UserContext();
		userContext.setTenantId(UserContextProvider.getCurrentTenantId());
		userContext.setDealerId(dealerId);
		userContext.setUserId(UserContextProvider.getCurrentUserId());
		return  userContext;
	}


	public static Map<String, String> getSiteIdVsNameForCurrentDealer(GlobalService globalService){
		List<OemSiteDetailsDto> listOfOemSiteDetails = TCollectionUtils.nullSafeList(globalService.getOemSiteDetails());
		return listOfOemSiteDetails.stream().collect(Collectors.toMap(
				OemSiteDetailsDto::getSiteId, OemSiteDetailsDto::getName, (oldValue, newValue) -> newValue));
	}

	public static Map<String, String> getDealerIdVsDealerNameForTenant(String tenantId, GlobalService globalService){
		Map<String, String> dealerNameToIdMapping = new HashMap<>();
		List<DealerMaster> dealerMasters = globalService.getAllDealerDetailsForTenant(tenantId);
		dealerMasters.forEach(dealerMaster -> {
			dealerNameToIdMapping.put(dealerMaster.getId(),dealerMaster.getDealerName().trim());
		});
		return dealerNameToIdMapping;
	}
}
