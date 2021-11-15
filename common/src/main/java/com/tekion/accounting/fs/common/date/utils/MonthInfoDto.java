package com.tekion.accounting.fs.common.date.utils;

import com.tekion.accounting.fs.common.slackAlert.DealerInfoDto;
import lombok.Data;

@Data
public class MonthInfoDto extends DealerInfoDto {
	Integer month;// should be from 0-11
	Integer year;

	boolean needPostAheadCalculation;
	private boolean ignoreSchedulesMarkedUnfixable=true;

}
