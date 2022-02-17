package com.tekion.accounting.fs.dealerMigration.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class AthenaUtils {

	@Getter
	@RequiredArgsConstructor
	public enum AthenaDatePattern {
		YYYY_MM_DD("yyyy-MM-dd"),
		MM_DD_YY("MM/dd/yy");
		private final String datePattern;
	}
}

