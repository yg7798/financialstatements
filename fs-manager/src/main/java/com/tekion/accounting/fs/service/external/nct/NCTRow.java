package com.tekion.accounting.fs.service.external.nct;

import com.tekion.accounting.fs.integration.Detail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NCTRow {
	public static String HEADER_LOCATION = "LOCATION";
	public static String HEADER_AMOUNT= "AMOUNT";
	public static String HEADER_MMYY = "MMYY";

	private String location;
	private String amount;
	private String mmyy;

	public static NCTRow getHeaderRow(){
		return new NCTRow(HEADER_LOCATION, HEADER_AMOUNT, HEADER_MMYY);
	}
	public static NCTRow getNctRow(Detail detail, String date){
		NCTRow nctRow = new NCTRow();
		if(Objects.nonNull(detail)){
			nctRow.setAmount(detail.getAccountValue());
			nctRow.setLocation(detail.getAccountId());
			nctRow.setMmyy(date);
		}
		return nctRow;
	}
}
