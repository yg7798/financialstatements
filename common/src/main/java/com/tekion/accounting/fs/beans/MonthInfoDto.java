package com.tekion.accounting.fs.beans;

import com.tekion.accounting.fs.slackDataAlert.DealerInfoDto;
import lombok.Data;

@Data
public class MonthInfoDto extends DealerInfoDto {
	Integer month;// should be from 0-11
	Integer year;

	boolean needPostAheadCalculation;
	private boolean ignoreSchedulesMarkedUnfixable=true;

}
