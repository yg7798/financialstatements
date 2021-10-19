package com.tekion.accounting.fs.beans;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MigrationMetaDataForFsEntry {
	String tkFsStatementNumber;
}
