package com.tekion.accounting.fs.common.utils;

import com.tekion.accounting.fs.common.date.utils.MonthInfoDto;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.experimental.UtilityClass;

import java.util.Calendar;
import java.util.Objects;

@UtilityClass
public class MonthUtils {


	public static MonthInfo getCopy(MonthInfo monthInfoReq){

		if(Objects.isNull(monthInfoReq)){
			return null;
		}
		MonthInfo monthInfo = new MonthInfo();
		monthInfo.setMonth(monthInfoReq.getMonth());
		monthInfo.setYear(monthInfoReq.getYear());
		return monthInfo;
	}
	public static MonthInfo getMonthInfo(int year, int month){

		MonthInfo monthInfo = new MonthInfo();
		monthInfo.setMonth(month);
		monthInfo.setYear(year);
		return monthInfo;

	}

	public static MonthInfo getPrevMonthInfo(int year, int month){

		MonthInfo monthInfo = new MonthInfo();
		if(month==0){
			monthInfo.setMonth(Calendar.DECEMBER);
			monthInfo.setYear(year - 1);
		}
		else{
			monthInfo.setMonth(month-1);
			monthInfo.setYear(year);
		}
		return monthInfo;
	}

	//takes input and return on 0-11 scale
	public static MonthInfo getPrevMonthInfo(MonthInfo giveInfo){

		return getPrevMonthInfo(giveInfo.getYear(),giveInfo.getMonth());
	}

	//takes input and return on 1-12 scale
	public static MonthInfo getPrevMonthInfo_1_12_scale(MonthInfo giveInfo){
		MonthInfo monthInfo = convertTo_0_11_Scale(giveInfo);
		return convertTo_1_12_Scale(getPrevMonthInfo(monthInfo.getYear(),monthInfo.getMonth()));
	}

	//1-12Scale
	public static MonthInfo getNextMonthInfo_1_12_scale(MonthInfo giveInfo){
		MonthInfo monthInfo = convertTo_0_11_Scale(giveInfo);
		return convertTo_1_12_Scale(getNextMonthInfo(monthInfo));
	}

	//0-11 Scale
	public static MonthInfo getNextMonthInfo(MonthInfo giveInfo){

		MonthInfo monthInfo = new MonthInfo();
		if(giveInfo.getMonth().equals(Calendar.DECEMBER)){
			monthInfo.setMonth(0);
			monthInfo.setYear(giveInfo.getYear()+1);
		}
		else{
			monthInfo.setMonth(giveInfo.getMonth()+1);
			monthInfo.setYear(giveInfo.getYear());
		}
		return monthInfo;
	}

	public static MonthInfo convertTo_1_12_Scale(MonthInfo monthInfoReq){

		MonthInfo monthInfo = new MonthInfo();
		monthInfo.setMonth(monthInfoReq.getMonth()+1);
		monthInfo.setYear(monthInfoReq.getYear());
		return monthInfo;
	}

	public static MonthInfo convertTo_0_11_Scale(MonthInfo monthInfoReq){

		MonthInfo monthInfo = new MonthInfo();
		monthInfo.setMonth(monthInfoReq.getMonth()-1);
		monthInfo.setYear(monthInfoReq.getYear());
		return monthInfo;
	}

	public static MonthInfoDto convertToMonthInfoDto(MonthInfo monthInfo){

		MonthInfoDto toReturn = new MonthInfoDto();
		toReturn.setMonth(monthInfo.getMonth());
		toReturn.setYear(monthInfo.getYear());
		return toReturn;
	}

	public static int getNewYearStartMonth(){
		return Calendar.JANUARY;
	}


	public static boolean isPostAheadMonth(MonthInfo monthInfoToCheck, MonthInfo activeMonthInfo){
		if(Objects.isNull(monthInfoToCheck) || Objects.isNull(activeMonthInfo)){
			throw new TBaseRuntimeException();
		}

		if(activeMonthInfo.getMonth().equals(Calendar.DECEMBER)  && monthInfoToCheck.getYear().equals(activeMonthInfo.getYear()+1)
				&& monthInfoToCheck.getMonth().equals(Calendar.JANUARY)){
			return true;
		}

		if(monthInfoToCheck.getMonth()<=Calendar.DECEMBER
				&& monthInfoToCheck.getMonth().equals(activeMonthInfo.getMonth()+1)
				&& activeMonthInfo.getYear().equals(monthInfoToCheck.getYear())){
			return true;
		}
		return false;
	}


	public static boolean isActiveMonth(MonthInfo monthInfoToCheck, MonthInfo activeMonthInfo){
		if(Objects.isNull(monthInfoToCheck) || Objects.isNull(activeMonthInfo)){
			throw new TBaseRuntimeException();
		}
		return monthInfoToCheck.equals(activeMonthInfo);
	}


	public static boolean isHistoricalMonth(MonthInfo monthInfoToCheck, MonthInfo activeMonthInfo){
		if(Objects.isNull(monthInfoToCheck) || Objects.isNull(activeMonthInfo)){
			throw new TBaseRuntimeException();
		}
		if(isActiveMonth(monthInfoToCheck, activeMonthInfo)
				|| isPostAheadMonth(monthInfoToCheck, activeMonthInfo)
				|| isAfterPostAheadMonth(monthInfoToCheck, activeMonthInfo)){
			return false;
		}
		return true;
	}

	public static boolean isAfterPostAheadMonth(MonthInfo monthInfoToCheck, MonthInfo activeMonthInfo){
		if(Objects.isNull(monthInfoToCheck) || Objects.isNull(activeMonthInfo)){
			throw new TBaseRuntimeException();
		}

		if(activeMonthInfo.getMonth() == Calendar.DECEMBER && monthInfoToCheck.getYear().equals(activeMonthInfo.getYear()+1)
				&& monthInfoToCheck.getMonth()>Calendar.JANUARY){
			return true;
		}

		if(monthInfoToCheck.getMonth()<=Calendar.DECEMBER
				&& monthInfoToCheck.getMonth()> (activeMonthInfo.getMonth()+1)
				&& activeMonthInfo.getYear().equals(monthInfoToCheck.getYear())){
			return true;
		}
		return false;
	}

	public static long getMonthDifference_0_11(MonthInfo monthInfoToCheck, MonthInfo monthInfoToCheckAgainst) {
		int signMultiplierFactor = 1;
		if(monthInfoToCheck.equals(monthInfoToCheckAgainst)){
			return 0;
		}

		if(MonthInfo.isGivenMonthGTEMonth(monthInfoToCheck,monthInfoToCheckAgainst)){
			signMultiplierFactor = -1;
		}
		MonthInfo smallerMonth = signMultiplierFactor>0 ? monthInfoToCheck : monthInfoToCheckAgainst;
		MonthInfo largerMonth = signMultiplierFactor>0 ? monthInfoToCheckAgainst: monthInfoToCheck  ;
		int counter=0;

		MonthInfo smallerMonthCopy = getCopy(smallerMonth);

		while (MonthInfo.isGivenMonthLTEMonthToCheckAgainst(smallerMonthCopy,largerMonth)){
			counter++;
			smallerMonthCopy = getNextMonthInfo(smallerMonthCopy);
		}
		return (counter-1)*signMultiplierFactor;
	}
}
