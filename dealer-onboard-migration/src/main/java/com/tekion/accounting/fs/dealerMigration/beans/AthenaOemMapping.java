package com.tekion.accounting.fs.dealerMigration.beans;

import lombok.Data;


@Data
public class AthenaOemMapping {

	private String oemStatementFormat;
	private String pclCode;
	private String oemStatementVersion;
	private String dmsAccountNumber;
	private String oemAccountNumber;
	private String siteId;
	private String fsStatementNumber;
}
