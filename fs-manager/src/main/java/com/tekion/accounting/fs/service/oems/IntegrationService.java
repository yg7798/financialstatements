package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.integration.masterdata.beans.ProviderInfoRequest;
import com.tekion.integration.masterdata.response.ProviderDealerMappingInfoResponse;
import com.tekion.integration.masterdata.services.ProviderDealerMappingInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IntegrationService {

	@Autowired
	protected ProviderDealerMappingInfoService providerDealerMappingInfoService;

	public String getBacCodeFromIntegration(String oemCode, String arcSiteId, String brand){
		ProviderInfoRequest req = new ProviderInfoRequest();
		req.setDealerId(UserContextProvider.getCurrentDealerId());
		req.setTenantId(UserContextProvider.getCurrentTenantId());
		req.setArcSiteId(arcSiteId);
		req.setOemCode(oemCode);
		req.setBrandName(brand);

		try{
			log.info("getBacCodeFromIntegration req {}", JsonUtil.toJson(req));
			ProviderDealerMappingInfoResponse res = providerDealerMappingInfoService.getProviderDealerMappingInfo(req);
			log.info("FS BAC code response integration {}", JsonUtil.toJson(res));
			return res.getProviderDealerId();
		}catch (Exception e){
			log.error(String.format("Error while fetching BAC code for dealer %s %s Financial Statement", UserContextProvider.getCurrentDealerId(), oemCode), e);
		}
		return null;
	}
}
