package com.tekion.accounting.fs.common.slackAlert;

import com.tekion.accounting.fs.common.pod.DealerInfo;
import lombok.Data;

import java.util.List;

@Data
public class DealerInfoDto {
	private List<DealerInfo> dealerInfoList;
	private boolean includeQaDealers;
	private boolean includeDecommissionedDealers;
}
