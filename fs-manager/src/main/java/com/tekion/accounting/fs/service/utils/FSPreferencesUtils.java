package com.tekion.accounting.fs.service.utils;

import com.tekion.accounting.fs.beans.accountingInfo.FSPreferences;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class FSPreferencesUtils {

	public static void  enableRoundedTrialBal(FSPreferences fsPreferences, String oem){
		Map<String, Boolean> map = fsPreferences.getUseRoundedTrialBal();
		if(Objects.isNull(map)){
			map = new HashMap<>();
			fsPreferences.setUseRoundedTrialBal(map);
		}
		map.put(oem, true);
	}

	public static void disableRoundedTrialBal(FSPreferences fsPreferences, String oem){
		Map<String, Boolean> map = fsPreferences.getUseRoundedTrialBal();
		if(Objects.isNull(map) || Objects.isNull(map.get(oem))){
			return;
		}
		map.put(oem, false);
	}

}
