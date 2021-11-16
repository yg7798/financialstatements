package com.tekion.accounting.fs.dto.cellcode;

import lombok.Data;

@Data
public class CellCodeSnapshotCreateDto {
	private String fsId;
	private Integer month;
	private Integer oemFsYear;
	private boolean includeM13;
	private boolean addM13BalInDecBalances;
}
