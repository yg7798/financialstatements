package com.tekion.accounting.fs.beans;

import lombok.Builder;

@Builder
public class DateFilter {
	private long startDate;
	private long endDate;

	public DateFilter() {
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	public DateFilter(long startDate, long endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
