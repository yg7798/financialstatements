package com.tekion.accounting.fs.common.enums;

import lombok.Getter;

@Getter
public enum Month {
	JAN(1, "January","Jan"),
	FEB(2, "February","Feb"),
	MAR(3, "March","Mar"),
	APR(4, "April","Apr"),
	MAY(5, "May","May"),
	JUN(6, "June","Jun"),
	JUL(7, "July","Jul"),
	AUG(8, "August","Aug"),
	SEP(9, "September","Sep"),
	OCT(10, "October","Oct"),
	NOV(11, "November","Nov"),
	DEC(12, "December","Dec");

	private int monthNo;
	private String displayName;
	private String shortDisplayLabel;

	Month(int no, String displayName) {
		this.monthNo = no;
		this.displayName = displayName;
	}

	Month(int no, String displayName, String shortDisplayLabel) {
		this.monthNo = no;
		this.displayName = displayName;
		this.shortDisplayLabel = shortDisplayLabel;
	}
	public static int byName(String monthName){
		for(Month month:Month.values()){
			if(month.name().equals(monthName)){
				return month.getMonthNo();
			}
		}
		return -1;
	}

	public static String byNumber(int monthNo) {
		for(Month month : Month.values()) {
			if(month.getMonthNo() == monthNo) {
				return month.name();
			}
		}
		return null;
	}

	public static Month monthByNumber(int monthNo) {
		for(Month month : Month.values()) {
			if(month.getMonthNo() == monthNo) {
				return month;
			}
		}
		return null;
	}
}
