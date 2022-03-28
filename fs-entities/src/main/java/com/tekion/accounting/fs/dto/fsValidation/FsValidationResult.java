package com.tekion.accounting.fs.dto.fsValidation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FsValidationResult {
	private String cellCode;
	private String ruleId;
}
