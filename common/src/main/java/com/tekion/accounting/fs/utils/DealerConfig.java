package com.tekion.accounting.fs.utils;

import com.google.common.cache.LoadingCache;
import com.tekion.admin.beans.dealersetting.DealerMaster;
import com.tekion.core.properties.TekionCommonProperties;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.TimeZone;

import static java.util.Objects.isNull;

@Component
@Data
@Slf4j
public class DealerConfig {

	public static String PO_BOX = "PO BOX";

	private static String TIME_ZONE_FOR_LOCAL_TESTING = "America/Los_Angeles";

	@Autowired
	@Qualifier("dealerCache")
	private LoadingCache<String, DealerMaster> dealerCache;


	public DealerMaster getDealerMaster() {
		DealerMaster dealerMaster = null;
		try {
			dealerMaster = dealerCache.get(getDealerUniqueKey());
		} catch (Exception e) {
			// todo, is it okay?
		}
		return isNull(dealerMaster) ? new DealerMaster() : dealerMaster;
	}

	public void invalidateDealerCache() {
		dealerCache.invalidateAll();
	}


	public String getDealerTimeZoneName() {
		if(isLocalClusterType()){
			return TIME_ZONE_FOR_LOCAL_TESTING;//TODO : How to handle fallback ?
		}
		else{
			return getDealerMaster().getTimeZone();
		}
	}

	public TimeZone getDealerTimeZone() {
		return TimeZone.getTimeZone(getDealerTimeZoneName());
	}

	public ZoneId getDealerZoneId() {
		return ZoneId.of(getDealerTimeZoneName());
	}


	public String getDealerCountryCode() { return getDealerMaster().getDealerCountryCode(); }

	public String getDealerCityState() {
		try{
			return getDealerMaster().getDealerAddress().get(0).getCity()+", "+getDealerMaster().getDealerAddress().get(0).getState();
		}catch (Exception e){
			log.error("Error while reading dealer city from dealer master {}", e.getMessage());
		}
		return "";
	}

	public String getDealerPostBox() {
		try{
			return PO_BOX + " "+getDealerMaster().getDealerAddress().get(0).getZipCode();
		}catch (Exception e){
			log.error("Error while reading dealer post box from dealer master {}", e.getMessage());
		}
		return "";

	}

	public String getDealerName() {
		return getDealerMaster().getDealerName();
	}

	public static void updateLocalTimeZone(String timeZone){
		TIME_ZONE_FOR_LOCAL_TESTING = timeZone;
	}

	public static String getDealerUniqueKey() {
		TStringUtils.KeyStringBuilder keyStringBuilder = TStringUtils.KeyStringBuilder.newBuilder("_");
		return keyStringBuilder.append(UserContextProvider.getCurrentTenantId())
				.append(UserContextProvider.getCurrentDealerId())
				.append(UserContextUtils.getSiteIdFromUserContext()).toString();
	}

	public static boolean isLocalClusterType(){
		return "local".equals(System.getenv(TekionCommonProperties.CLUSTER_TYPE));
	}
}
