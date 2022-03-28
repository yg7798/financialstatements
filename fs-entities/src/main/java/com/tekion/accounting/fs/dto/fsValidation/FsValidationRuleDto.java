package com.tekion.accounting.fs.dto.fsValidation;

import com.poiji.annotation.ExcelCell;
import com.tekion.accounting.fs.beans.fsValidation.FsValidationRule;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FsValidationRuleDto {
	@NotEmpty
	@ExcelCell(0)
	private String oemCode;
	@ExcelCell(1)
	private String line;
	@NotEmpty
	@ExcelCell(2)
	private String errorCode;
	@ExcelCell(3)
	private String type;
	@ExcelCell(4)
	private String page;
	@ExcelCell(5)
	private String description;
	@NotEmpty
	@ExcelCell(6)
	private String expression;
	private String oemId;
	private Integer year;
	private String country;

	public FsValidationRule toFsValidationRule(String oemId, Integer year, String country){
		FsValidationRule rule = FsValidationRule.builder().oemCode(oemCode).description(description).errorCode(errorCode).page(page)
				.line(line).type(type).expression(expression).oemId(oemId).country(country).year(year)
				.build();
		rule.setCreatedTime(System.currentTimeMillis());
		rule.setModifiedTime(System.currentTimeMillis());
		return  rule;
	}
}
