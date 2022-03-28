package com.tekion.accounting.fs.beans.fsValidation;

import com.tekion.accounting.fs.dto.fsValidation.FsValidationResult;
import com.tekion.core.beans.TBaseMongoBean;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndexes({
		@CompoundIndex(
				name = "oemId_country_year_oemCode_errorCode",
				def = "{'oemId':1, 'country':1, 'year':1, 'oemCode':1, 'errorCode': 1}", unique = true
		)
})
public class FsValidationRule extends TBaseMongoBean {

	public static String ERROR_CODE = "errorCode";

	private String oemCode;
	private String errorCode;
	private String description;
	private String page;
	private String line;
	private String type;
	private String expression;
	private Integer year;
	private String country;
	private String oemId;

	public FsValidationResult getValidationResult(String cellCode, FsValidationRule rule){
		FsValidationResult result = new FsValidationResult();
		result.setCellCode(cellCode);
		result.setRuleId(rule.getId());
		return result;
	}
}
