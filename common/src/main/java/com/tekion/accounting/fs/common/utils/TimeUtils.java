package com.tekion.accounting.fs.common.utils;


import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.common.date.utils.DateFilter;
import com.tekion.accounting.fs.common.date.utils.MonthYear;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TimeUtils {

	public static final String DD_MM_YYYY = "dd/MM/yyyy";
	public static final String MM_DD_YYYY = "MM/dd/yyyy";
	private static DealerConfig dealerConfig;

	@Autowired
	public TimeUtils(DealerConfig dealerConfig){
		this.dealerConfig = dealerConfig;
	}


	public static Long getDateStartFromEpoch(Long input){
		Date inputDate = new Date(input) ;
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		String formatted = format.format(inputDate);
		try {
			Date outputDate = format.parse(formatted);
			return outputDate.getTime();
		} catch (ParseException e) {
			log.error("Exception while TimeUtils getDateFromEpoch : {} ", (Object) e.getStackTrace());
			return input;
		}
	}

	public static Long getDateEnd(Long input){
		Date inputDate = new Date(input) ;
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		String formatted = format.format(inputDate);
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(format.parse(formatted));
			c.add(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.MILLISECOND,-1);
			return c.getTime().getTime();
		} catch (ParseException e) {
			log.error("Exception while TimeUtils getDateEnd : {} ", (Object) e.getStackTrace());
			return input;
		}
	}

	public static Long getNextDateStart(Long input){
		Date inputDate = new Date(input) ;
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		String formatted = format.format(inputDate);
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(format.parse(formatted));
			c.add(Calendar.DAY_OF_MONTH, 1);
			return c.getTime().getTime();
		} catch (ParseException e) {
			log.error("Exception while TimeUtils getNextDateStart : {} ", (Object) e.getStackTrace());
			return input;
		}
	}

	public static Long getMonthsStart(Long input){
		Date inputDate = new Date(input) ;
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		String formatted = format.format(inputDate);
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(format.parse(formatted));
			c.set(Calendar.DAY_OF_MONTH, 1);
			return c.getTime().getTime();
		} catch (ParseException e) {
			return input;
		}
	}

	public static Long getEpochAtDayOfMonth(Long input, int dayOfMonth){
		Date inputDate = new Date(input) ;
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		String formatted = format.format(inputDate);
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(format.parse(formatted));
			c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			return c.getTime().getTime();
		} catch (ParseException e) {
			log.error("Parse Exception: ", e);
			return input;
		}
	}

	public static Long getYearStart(Long input) {
		Date inputDate = new Date(input);
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		String formatted = format.format(inputDate);
		try {
			Calendar c = Calendar.getInstance();
			c.setTimeZone(dealerConfig.getDealerTimeZone());
			c.setTime(format.parse(formatted));
			c.set(Calendar.DAY_OF_YEAR, 1);
			return c.getTime().getTime();
		} catch(ParseException e) {
			log.error("Exception while TimeUtils getYearStart : {} ", (Object) e.getStackTrace());
			return input;
		}
	}

	public static String getStringFromEpoch(Long input) {
		Date inputDate = new Date(input) ;
		DateFormat format = new SimpleDateFormat(MM_DD_YYYY);
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		return format.format(inputDate);
	}

	public static String getStringFromEpoch(Long input, String pattern) {
		Date inputDate = new Date(input) ;
		DateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		return format.format(inputDate);
	}

	public static long getEpochFromString(String input, String pattern) {
		DateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		Date inputDate = null;
		try {
			inputDate = format.parse(input);
		} catch (ParseException e) {
			log.error("Not able to parse date");
		}
		return inputDate.getTime();
	}

	public static boolean isSameDay(Long input1, Long input2){
		return getDateStartFromEpoch(input1).equals(getDateStartFromEpoch(input2)) ;
	}

	public static Integer getCurrentQuartersStartMonth() {
		return getCurrentMonth() - getCurrentMonth()%3;
	}

	public static Integer getCurrentQuartersStartMonthsYear() {
		Calendar c = getCalendarInstance(null, true);
		return c.get(Calendar.YEAR);
	}

	public static Integer getLastQuartersStartMonth() {
		Calendar c = getCalendarInstance(null, getCurrentQuartersStartMonth(), null, true);
		offsetCalendarAndReturn(c,0,-3,0);
		return c.get(Calendar.MONTH);
	}

	public static Integer getLastQuartersStartMonthsYear() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(),true);
		c.set(Calendar.MONTH, getCurrentQuartersStartMonth());
		c.add(Calendar.MONTH, -3);
		return c.get(Calendar.YEAR);
//        Calendar c = getCalendarInstance(null, getCurrentQuartersStartMonth(), null, true);
//        offsetCalendarAndReturn(c,0,-3,0);
//        return c.get(Calendar.YEAR);
	}

	public static Integer getLastToLastQuartersStartMonth() {
		Calendar c = getCalendarInstance(null, getCurrentQuartersStartMonth(), null, true);
		offsetCalendarAndReturn(c,0,-6,0);
		return c.get(Calendar.MONTH);
	}

	public static Integer getLastToLastQuartersStartMonthsYear() {
		Calendar c = getCalendarInstance(null, getCurrentQuartersStartMonth(), null, true);
		offsetCalendarAndReturn(c,0,-6,0);
		return c.get(Calendar.YEAR);
	}

	public static Integer getLastQuartersEndMonth() {
		Calendar c = getCalendarInstance(null, getCurrentQuartersStartMonth(), null, true);
		offsetCalendarAndReturn(c,0,-1,0);
		return c.get(Calendar.MONTH);
	}

	public static Integer getLastQuartersEndMonthsYear() {
		Calendar c = getCalendarInstance(null, getCurrentQuartersStartMonth(), null, true);
		offsetCalendarAndReturn(c,0,-1,0);
		return c.get(Calendar.YEAR);
	}

	public static Integer getCurrentYear() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(),true);
		return c.get(Calendar.YEAR);
	}

	public static Integer getYearForNextMonthInDealerTimeZone() {
		Integer currentYear= getCurrentYearInDealerTimezone();

		Integer postAheadMonth = getNextMonthInDealerTimeZone();
		Integer postAheadMonthsYear;
		if(postAheadMonth!=0){
			postAheadMonthsYear = currentYear;
		}
		else {
			postAheadMonthsYear = currentYear+1;
		}

		return postAheadMonthsYear;
	}

	public static Integer getCurrentMonth() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(),true);
		return c.get(Calendar.MONTH);
	}

	public static Integer getCurrentMonthsYear() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(),true);
		return c.get(Calendar.YEAR);
	}

	public static Integer getPreviousMonth() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(),true);
		offsetCalendarAndReturn(c,0,-1,0);
		return c.get(Calendar.MONTH);
	}

	public static Integer getPreviousMonthInDealerTimeZone() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(),true);
		offsetCalendarAndReturn(c,0,-1,0);
		return c.get(Calendar.MONTH);
	}

	public static Integer getPreviousMonthsYear() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(),true);
		offsetCalendarAndReturn(c,0,-1,0);
		return c.get(Calendar.YEAR);
	}

	public static Integer getPreviousMonthsYearInDealerTimeZone() {
		Integer currentYear= getCurrentYearInDealerTimezone();

		Integer prevMonth = getPreviousMonthInDealerTimeZone();
		Integer previousMonthsYear;
		if(prevMonth!=11){
			previousMonthsYear = currentYear;
		}
		else {
			previousMonthsYear = currentYear-1;
		}

		return previousMonthsYear;
	}

	public static Integer getPreviousToPrevMonth() {
		Calendar c = getCalendarInstance(null,true);
		c.add(Calendar.MONTH, -2);
		return c.get(Calendar.MONTH);
	}

	public static Integer getPreviousToPrevMonthsYear() {
		Calendar c = getCalendarInstance(null,true);
		c.add(Calendar.MONTH, -2);
		return c.get(Calendar.YEAR);
	}


	/**
	 * month range is {1,....,12}
	 * */
	public static YearMonth decrementMonth(YearMonth yearMonth) {

		int month = yearMonth.getMonthValue();
		int year = yearMonth.getYear();

		month -= 1;

		if(month == 0){
			month = 12;
			year -= 1;
		}

		return YearMonth.of(year, month);
	}

	public static long getMonthsStartTime(Integer reqYear, Integer reqMonth) {
		Calendar c = new Calendar.Builder().set(Calendar.YEAR,reqYear).set(Calendar.MONTH,reqMonth).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c.getTime().getTime();
	}

	public static long getMonthsEndTime(Integer reqYear, Integer reqMonth) {
		Calendar c = new Calendar.Builder().set(Calendar.YEAR,reqYear).set(Calendar.MONTH,reqMonth+1).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime().getTime();
	}

	/*
		month scale: 0-11 (January = 0)
	 */
	public static long getPostAheadMonthsEndTime(MonthInfo monthInfo){
		long postAheadMonthEndTime;
		if (monthInfo.getMonth() == Calendar.DECEMBER) {
			postAheadMonthEndTime = TimeUtils.getMonthsEndTime(monthInfo.getYear() + 1, Calendar.JANUARY);
		} else {
			postAheadMonthEndTime = TimeUtils.getMonthsEndTime(monthInfo.getYear(), monthInfo.getMonth() + 1);
		}
		return postAheadMonthEndTime;
	}

	public static long getDateStartTimeInYearOffset(long timeToUse, int yearOffset) {
		Calendar calendarOfRequestedDate = getCalendarInstance(timeToUse, true);
		offsetCalendarAndReturn(calendarOfRequestedDate,yearOffset,0,0);
		return getDaysStartTime(calendarOfRequestedDate.get(Calendar.YEAR), calendarOfRequestedDate.get(Calendar.MONTH), calendarOfRequestedDate.get(Calendar.DAY_OF_MONTH));
	}

	public static long getDateEndTimeInYearOffset(long timeToUse, int yearOffset) {
		Calendar calendarOfRequestedDate = getCalendarInstance(timeToUse, true);
		offsetCalendarAndReturn(calendarOfRequestedDate,yearOffset,0,0);
		return getDaysEndTime(calendarOfRequestedDate.get(Calendar.YEAR), calendarOfRequestedDate.get(Calendar.MONTH), calendarOfRequestedDate.get(Calendar.DAY_OF_MONTH));
	}

	public static YearMonth getFYStartDetails(int requestedYear, int requestedMonth_0_11, int fiscalStartMonth_0_11){
		int fiscalStartYear = requestedYear;
		if(requestedMonth_0_11 < fiscalStartMonth_0_11){
			fiscalStartYear = fiscalStartYear-1;
		}
		return YearMonth.of(fiscalStartYear, Month.of(fiscalStartMonth_0_11+1));
	}

	public static long getPreviousYearTime() {
		Calendar now = getCalendarInstance(null,true);
		offsetCalendarAndReturn(now,-1,0,0);
		return now.getTimeInMillis();
	}

	public static long getPreviousYearTimeFromGivenTime(long timeToUse) {
		Calendar now = getCalendarInstance(timeToUse,true);
		offsetCalendarAndReturn(now,-1,0,0);
		return now.getTimeInMillis();
	}

	public static long getYearEndTime(Integer reqYear) {
		Calendar c = new Calendar.Builder().set(Calendar.YEAR,reqYear+1).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime().getTime();
	}

	public static long getYearStartTime(Integer reqYear) {
		Calendar c = new Calendar.Builder().set(Calendar.YEAR,reqYear).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c.getTime().getTime();
	}

	public static long getCurrentMonthsStartTime() {
		Calendar instance = getCalendarInstance(getCurrentSystemTime(),true);
		Integer currnetYear = instance.get(Calendar.YEAR);
		Integer currentMonth = instance.get(Calendar.MONTH);
		Calendar c = new Calendar.Builder().set(Calendar.YEAR,currnetYear).set(Calendar.MONTH,currentMonth).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c.getTime().getTime();
	}

	public static long getPrevMonthsStartTime() {
		Integer prevYear = getPreviousMonthsYearInDealerTimeZone();
		Integer prevMonth = getPreviousMonthInDealerTimeZone();
		Calendar c = new Calendar.Builder().set(Calendar.YEAR,prevYear).set(Calendar.MONTH,prevMonth).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c.getTime().getTime();
	}

	public  static long getPrevMonthsEndTime() {
		return getCurrentMonthsStartTime() - 1;
	}

	public static long getCurrentQuartersStartTime() {
		Integer currentQuartesYear = getCurrentQuartersStartMonthsYear();
		Integer currentQuartesMonth = getCurrentQuartersStartMonth();
		Calendar c = new Calendar.Builder().set(Calendar.YEAR,currentQuartesYear).set(Calendar.MONTH,currentQuartesMonth).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c.getTime().getTime();
	}

	public static long getLastQuartersStartTime() {
		Integer lastQuartesYear = getLastQuartersStartMonthsYear();
		Integer lastQuartesMonth = getLastQuartersStartMonth();
		Calendar c = new Calendar.Builder().set(Calendar.YEAR,lastQuartesYear).set(Calendar.MONTH,lastQuartesMonth).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c.getTime().getTime();
	}

	public static long getLastQuartersEndTime() {
		return getCurrentQuartersStartTime() - 1;
	}

	public static long getCurrentYearsStartTime() {
		Integer currentYear = getCurrentMonthsYear();
		Calendar c = new Calendar.Builder().set(Calendar.YEAR,currentYear).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c.getTime().getTime();
	}

	public static long getEpochFromStringWithTimeZone(String input, String pattern) {
		DateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		Date inputDate = null;
		try {
			inputDate = format.parse(input);
		} catch (ParseException e) {
			log.error("Not able to parse date for : {} ", input);
		}
		return inputDate.getTime();
	}

	public static long getEpochFromStringWithoutTimeZone(String input, String pattern) {
		DateFormat format = new SimpleDateFormat(pattern);
		Date inputDate = null;
		try {
			inputDate = format.parse(input);
		} catch (ParseException e) {
			log.error("Not able to parse date for : {} ", input);
		}
		return inputDate.getTime();
	}

	public static long getDayStartTime(Integer reqYear, Integer reqMonth, int date) {
		Calendar c = new Calendar.Builder().setDate(reqYear, reqMonth, date).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c.getTime().getTime();
	}

	public static int getMonthFromEpochInDealerTimezone(long timeInMilliSec){
		Calendar cal = getCalendarInstance(timeInMilliSec, true);
		return cal.get(Calendar.MONTH);
	}

	public static int getDayFromEpochInDealerTimezone(long timeInMilliSec){
		Calendar cal = getCalendarInstance(timeInMilliSec, true);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	public static int getYearFromEpochInDealerTimezone(long timeInMilliSec){
		Calendar cal = getCalendarInstance(timeInMilliSec, true);
		return cal.get(Calendar.YEAR);
	}

	public static int getNextMonth(int month){
		validateCalenderMonth(month);
		return month == Calendar.DECEMBER ? Calendar.JANUARY : month + 1;
	}

	public static int getPrevMonth(int month){
		validateCalenderMonth(month);
		return month == Calendar.JANUARY ? Calendar.DECEMBER : month - 1;
	}

	private static void validateCalenderMonth(int month){
		if(month < Calendar.JANUARY || month > Calendar.DECEMBER){
			throw new DateTimeException("invalid month!");
		}
	}

	public static Integer getCurrentMonthInDealerTimezone() {
		return Calendar.getInstance(dealerConfig.getDealerTimeZone()).get(Calendar.MONTH);
	}

	public static Integer getCurrentYearInDealerTimezone() {
		return getCalendarInstance(getCurrentSystemTime(),true).get(Calendar.YEAR);
	}

	public static Integer getCurrentDayInDealerTimezone() {
		return Calendar.getInstance(dealerConfig.getDealerTimeZone()).get(Calendar.DATE);
	}

	public static long getDaysEndTime(Integer reqYear, Integer reqMonth, int date) {
		Calendar c = new Calendar.Builder().setDate(reqYear, reqMonth, date + 1)
				.setTimeZone(dealerConfig.getDealerTimeZone()).build();
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime().getTime();
	}


	public static long getDaysStartTime(Integer reqYear, Integer reqMonth, int date) {
		Calendar c = new Calendar.Builder().setDate(reqYear, reqMonth, date)
				.setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c.getTime().getTime();
	}

	public static String getTimeInDateFormatFromEpoch(Long dateToFormat, String dateFormatPattern){
		Date date = new Date(dateToFormat);
		DateFormat format = new SimpleDateFormat(dateFormatPattern);
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		return format.format(date);
	}

	public static int getCurrentMonthLastDay(){
		Calendar c =Calendar.getInstance();
		c.setTimeZone(dealerConfig.getDealerTimeZone());
		return c.getActualMaximum(Calendar.DATE);
	}

	public static Integer getNextMonthInDealerTimeZone() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(), true);
		offsetCalendarAndReturn(c,0,1,0);
		return c.get(Calendar.MONTH);
	}

	public static Integer getNextYear() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(), true);
		offsetCalendarAndReturn(c,1,0,0);
		return c.get(Calendar.YEAR);
	}

	public static long getNextYearFromGivenTime(Long timeToUse) {
		Calendar c = getCalendarInstance(timeToUse, true);
		offsetCalendarAndReturn(c,1,0,0);
		return c.getTime().getTime();
	}

	public static Integer getPreviousYear() {
		Calendar c = getCalendarInstance(getCurrentSystemTime(), true);
		offsetCalendarAndReturn(c,-1,0,0);
		return c.get(Calendar.YEAR);
	}

	public static int getDayFromEpoch(Long invoiceDate) {
		Date date = new  Date(invoiceDate);
		DateFormat format = new SimpleDateFormat("dd");
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		return Integer.parseInt(format.format(date));
	}

	public static Integer getMonthFromEpoch(Long date) {
		Calendar c = getCalendarInstance(date, true);
		return c.get(Calendar.MONTH);
	}



	public static Integer getNextMonthFromEpoch(Long date) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(date));
		c.setTimeZone(dealerConfig.getDealerTimeZone());
		c.add(Calendar.MONTH, 1);
		return c.get(Calendar.MONTH);
	}

	public static long offSetEpochByGivenMonthInDealerTimeZone(Long date, int month) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(date));
		c.setTimeZone(dealerConfig.getDealerTimeZone());
		c.add(Calendar.MONTH, month);
		return c.getTime().getTime();
	}

	public static Integer getPreviousMonthFromEpoch(Long date) {
		Calendar c = getCalendarInstance(date, true);
		offsetCalendarAndReturn(c,0,-1,0);
		return c.get(Calendar.MONTH);
	}

	public static long subtractDaysFromEpoch(long epoch, int days) {
		Calendar c = getCalendarInstance(epoch, true);
		offsetCalendarAndReturn(c,0,0,-days);
		return c.getTime().getTime();
	}

	public static long subtractMinsFromEpoch(long epoch, int mins) {
		Calendar c = getCalendarInstance(epoch, true);
		c.add(Calendar.MINUTE, -mins);
		return c.getTime().getTime();
	}

	public static long addMinsToEpoch(long epoch, int mins) {
		Calendar c = getCalendarInstance(epoch, true);
		c.add(Calendar.MINUTE, mins);
		return c.getTime().getTime();
	}

	public static Month getLocalCurrentMonth(){
		return new Date().toInstant().atZone(ZoneId.of(dealerConfig.getDealerTimeZoneName()))
				.toLocalDate().getMonth();
	}

	public static int getLocalCurrentYear(){
		return new Date().toInstant().atZone(ZoneId.of(dealerConfig.getDealerTimeZoneName()))
				.toLocalDate().getYear();
	}

	public static long getLocalTodayStartTime(){
		return LocalDate.now(ZoneId.of(dealerConfig.getDealerTimeZoneName()))
				.atStartOfDay(ZoneId.of(dealerConfig.getDealerTimeZoneName())).toEpochSecond() * 1000;
	}

	public static long getLocalTodayEndTime(){
		return (LocalDate.now(ZoneId.of(dealerConfig.getDealerTimeZoneName())).plusDays(1)
				.atStartOfDay(ZoneId.of(dealerConfig.getDealerTimeZoneName())).toEpochSecond() * 1000)-1;
	}

	public static long getDateEndTimeInTimeZone(long epochTime) {
		Calendar build = getCalendarInstance(epochTime, true);
		return getDaysEndTime(build.get(Calendar.YEAR), build.get(Calendar.MONTH), build.get(Calendar.DAY_OF_MONTH));
	}

	public static long getDateStartTimeInTimeZone(long epochTime) {
		Calendar build = getCalendarInstance(epochTime, true);
		return getDaysStartTime(build.get(Calendar.YEAR), build.get(Calendar.MONTH), build.get(Calendar.DAY_OF_MONTH));
	}

	public static long getTimeInMillis(String date, String dateFormat){
		DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat)
				.withZone(DateTimeZone.forID(dealerConfig.getDealerTimeZoneName()));

		return formatter.parseDateTime(date).getMillis();
	}

	public static boolean isSameMonth(Long input1, Long input2){
		return getMonthFromEpochInDealerTimezone(input1) == (getMonthFromEpochInDealerTimezone(input2)) ;
	}


	public static Long getAge(Long minEpoch, Long maxEpoch) {
		double minEpochSOD = TimeUtils.getDateStartFromEpoch(minEpoch);
		double maxEpochSOD = TimeUtils.getDateStartFromEpoch(maxEpoch);
		return Math.round((maxEpochSOD - minEpochSOD) / TimeUnit.DAYS.toMillis(1));
	}

	public static long getTimezoneOffset(){
		return dealerConfig.getDealerTimeZone().getOffset(new Date().getTime());
	}

	public static long parseIsoDate(String isoDateString){
		TimeZone tz = TimeZone.getTimeZone(dealerConfig.getDealerTimeZoneName());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"); // Quoted "Z" to indicate UTC, no timezone offset
		df.setTimeZone(tz);
		try {
			Date  date  = df.parse(isoDateString);
			return date.getTime();
		} catch (ParseException e) {
			log.error("Parsing error {} ",isoDateString,e);
		}
		return new Date().getTime();
	}

	public static long parseInputAndGetTimeStamp(String isoDateString){
		TimeZone tz = TimeZone.getTimeZone(dealerConfig.getDealerTimeZoneName());
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy, hh:mm:ss.SSS aa"); // Quoted "Z" to indicate UTC, no timezone offset
		df.setTimeZone(tz);
		try {
			Date  date  = df.parse(isoDateString);
			return date.getTime();
		} catch (ParseException e) {
			log.error("Parsing error {} ",isoDateString,e);
			throw new TBaseRuntimeException();
		}
	}


	public static String getStringRepresentationTillMillis(long testing){
		String timeZone = dealerConfig.getDealerTimeZoneName();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy, hh:mm:ss.SSS aa");
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		return sdf.format(testing);

	}

	public static Calendar buildCalendar(int year, int month){

		Calendar c = new Calendar.Builder().set(Calendar.YEAR,year).set(Calendar.MONTH,month).setTimeZone(dealerConfig.getDealerTimeZone()).build();
		return c;
	}

	// returns on 0-11 scale
	public static MonthInfo getMonthInfoFromEpoch(long epochTime){
		Calendar calendar = buildCalendar(epochTime);
		MonthInfo monthInfo = MonthUtils.getMonthInfo(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
		return monthInfo;
	}

	public static int getDaysOfMonth(int year, int month){
		return buildCalendar(year, month).getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public static Calendar buildCalendar(long epoch){
		return getCalendarInstance(epoch,true);
	}

	public static String toSeconds(long milliSeconds){
		double d = milliSeconds/(double)1000;
		return d+" Seconds.";
	}

	public static Set<Integer> getMonthsBetween(int fromYear, int fromMonth, int toYear, int toMonth){

		int monthsCount = (toYear - fromYear) * 12 + toMonth - fromMonth + 1;

		Set<Integer> set = new HashSet<>();
		Calendar tempCalendar = Calendar.getInstance();
		tempCalendar.set(Calendar.MONTH, fromMonth);

		for( ;monthsCount-- > 0; ){
			set.add(tempCalendar.get(Calendar.MONTH));
			tempCalendar.roll(Calendar.MONTH, 1);
		}

		return set;
	}

	public static Long getEpochTimeWithOffsetDayInDealerTimeZone(Long date, int offset) {
		Date inputDate = new Date(date) ;
		DateFormat format = new SimpleDateFormat(DD_MM_YYYY);
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		String formatted = format.format(inputDate);
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(format.parse(formatted));
			c.add(Calendar.DAY_OF_MONTH, offset);
			return c.getTime().getTime();
		} catch (ParseException e) {
			log.error("Exception while calculating time with offset : ", e);
			return date;
		}
	}

	public static boolean isDateMatchedWithFormat(String input, String format) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(format);
			Date date = dateFormat.parse(input);
			if (input.equals(dateFormat.format(date))) {
				return true;
			}
		} catch (Exception ex) {
			return false;
		}
		return false;
	}


	private  static Calendar offsetCalendarAndReturn(Calendar calendar, int yearOffset, int monthOffset, int dayOffset) {
		doYearOffsetOnCalendar(calendar,yearOffset);
		doMonthOffsetOnCalendar(calendar,monthOffset);
		doDayOffsetOnCalendar(calendar,dayOffset);
		return calendar;

	}

	private static void doYearOffsetOnCalendar(Calendar calendarInstanceForTimeStamp, int yearOffset) {
		YearMonth lastYear = YearMonth.of( calendarInstanceForTimeStamp.get(Calendar.YEAR) + yearOffset, calendarInstanceForTimeStamp.get(Calendar.MONTH)+1);
		int daysInLastYearsMonth = lastYear.lengthOfMonth();

		/*
		 *  Handling Feb 29
		 * */

		if(daysInLastYearsMonth < calendarInstanceForTimeStamp.get(Calendar.DAY_OF_MONTH)){
			calendarInstanceForTimeStamp.set(Calendar.DAY_OF_MONTH, daysInLastYearsMonth);
		}
		calendarInstanceForTimeStamp.set(Calendar.YEAR, calendarInstanceForTimeStamp.get(Calendar.YEAR)+ yearOffset);
	}

	private static void doMonthOffsetOnCalendar(Calendar calendar, int monthOffset) {
		calendar.add(Calendar.MONTH, monthOffset);
	}

	private static void doDayOffsetOnCalendar(Calendar calendar, int dayOffset) {
		calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
	}


	/**
	 * send any param as null if doesnt have to be considered during calendar instantiation
	 * @param year
	 * @param month
	 * @param day
	 * @param considerTimeZone
	 */
	private static Calendar getCalendarInstance(Integer year, Integer month, Integer day, boolean considerTimeZone){
		Calendar calendar = getCalendarInstance(getCurrentSystemTime(),false);
		if(considerTimeZone){
			calendar.setTimeZone(dealerConfig.getDealerTimeZone());
		}
		return setDateDetailsInCalendar(year, month, day, calendar);
	}

	private static Calendar getCalendarInstance(Long timeToUse, boolean considerTimeZone){
		Calendar calendarOfRequestedDate = Calendar.getInstance();
		if(considerTimeZone){
			calendarOfRequestedDate.setTimeZone(dealerConfig.getDealerTimeZone());
		}
		if(Objects.nonNull(timeToUse)){
			calendarOfRequestedDate.setTime(new Date(timeToUse));
		}
		else {
			calendarOfRequestedDate.setTime(new Date(getCurrentSystemTime()));
		}
		return calendarOfRequestedDate;
	}

	public static Calendar getCalenderInstanceInDealerTimezone(){
		return Calendar.getInstance(dealerConfig.getDealerTimeZone());
	}



	private static Calendar setDateDetailsInCalendar(Integer year, Integer month, Integer day, Calendar calendar) {
		if(Objects.nonNull(year)){
			calendar.set(Calendar.YEAR,year);
		}
		if(Objects.nonNull(month)){
			calendar.set(Calendar.MONTH,month);
		}
		if(Objects.nonNull(day)){
			calendar.set(Calendar.DAY_OF_MONTH,day);
		}
		return calendar;
	}


	public static long getCurrentSystemTime(){
		return System.currentTimeMillis();
	}

	public static Date getDateInstance()
	{
		return new Date();
	}

	public static void validateTwelveIndexedMonth(int month){
		if(month < 1 || month > 12){
			throw new TBaseRuntimeException("invalid Month");
		}
	}

	public static long getStartTimeForPreviousYearMonth(long epoch) {
		Date inputDate = new Date(epoch) ;
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		format.setTimeZone(dealerConfig.getDealerTimeZone());
		String formatted = format.format(inputDate);
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(format.parse(formatted));
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.YEAR, c.get(Calendar.YEAR)-1);
			return c.getTime().getTime();
		} catch (ParseException e) {
			return epoch;
		}
	}

	// date parameter is in normal String form -- "20141012", SourceFormat - "yyyyMMdd"
	public static String convertDateInRequiredFormat(String date,String sourceFormat,String targetFormat){
		String formattedDate = null;
		try {
			Date parsedDate = new SimpleDateFormat(sourceFormat).parse(date);
			formattedDate = new SimpleDateFormat(targetFormat).format(parsedDate);
		} catch (ParseException e) {
			log.error("Not able to parse date : {} ", date);
		}
		return formattedDate;
	}

	//("2010-11-17 01:00 ", "dd-MM-yyyy HH:mm", TimeZone.getTimeZone("America/Los_Angeles"))
	public static long stringToMillis(final String date, String pattern, final TimeZone tz) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(tz);
		Date d = sdf.parse(date);
		return d.getTime();
	}

	public static String timeToDateString(final long milliSeconds, String pattern, final TimeZone tz){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(tz);
		Date date = new Date(milliSeconds);
		return sdf.format(date);
	}

	/*month -> 0-11 */
	public static long getNextMonthEndTime(Long epochTime) {
		Integer nextMonth = getNextMonthFromEpoch(epochTime);
		Integer year = getYearFromEpochInDealerTimezone(epochTime);
		if(nextMonth == 0){
			year ++;
		}
		return getMonthsEndTime(year, nextMonth);
	}

	/*month -> 0-11 */
	public static long getNextMonthStartTime(Long epochTime) {
		Integer nextMonth = getNextMonthFromEpoch(epochTime);
		Integer year = getYearFromEpochInDealerTimezone(epochTime);
		if(nextMonth == 0){
			year ++;
		}
		return getMonthsStartTime(year, nextMonth);
	}

	/*month -> 0-11 */
	public static MonthYear getMonthAndYearFromEpoch(Long date) {
		MonthYear monthYear = new MonthYear();
		Calendar c = getCalendarInstance(date, true);
		monthYear.setMonth(c.get(Calendar.MONTH));
		monthYear.setYear(c.get(Calendar.YEAR));
		return monthYear;
	}

	public static DateFilter getStartAndEndTimeDateFilterForPreviousDay() {
		int year = getCurrentYearInDealerTimezone();
		int month = getCurrentMonthInDealerTimezone();
		int day = getCurrentDayInDealerTimezone();
		return DateFilter.builder().
				startDate(getDaysStartTime(year, month, day - 1)).
				endDate(getDaysEndTime(year, month, day - 1)).
				build();
	}

	public static DateFilter getStartAndEndTimeDateFilterForCurrentDay() {
		int year = getCurrentYearInDealerTimezone();
		int month = getCurrentMonthInDealerTimezone();
		int day = getCurrentDayInDealerTimezone();
		return DateFilter.builder().
				startDate(getDaysStartTime(year, month, day)).
				endDate(getDaysEndTime(year, month, day)).
				build();
	}

	public static long getTimeWithTargetTimeZone(long time,String srcTimeZone,String targetTimeZone){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
		dateFormat.setTimeZone(TimeZone.getTimeZone(srcTimeZone));
		String endDate = dateFormat.format(new Date(time));
		dateFormat.setTimeZone(TimeZone.getTimeZone(targetTimeZone));
		try {
			Date parse = dateFormat.parse(endDate);
			return parse.getTime();
		} catch (ParseException e) {
			throw new TBaseRuntimeException("Timezone conversion issue", String.valueOf(time),srcTimeZone,targetTimeZone);
		}
	}

	// print zoned date and time in human readable format - Thursday, October 14, 2021 21:03:56 +0530
	public static String getCurrentDateTimeInReadableFormat() {
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
		java.time.format.DateTimeFormatter formatter =  java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy HH:mm:ss Z");
		return now.format(formatter);
	}

}

