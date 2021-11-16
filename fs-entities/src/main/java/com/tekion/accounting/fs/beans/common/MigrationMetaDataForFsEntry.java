package com.tekion.accounting.fs.beans.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MigrationMetaDataForFsEntry {
	String tkFsStatementNumber;
}
