package com.tekion.accounting.fs.dto.memo;

import com.tekion.accounting.fs.enums.OEM;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoWorksheetMigrateDto {
	@NotEmpty
	private OEM oemId;
	@NotEmpty
	private String country;
	@NonNull
	private Integer year;
	@NotEmpty
	private Set<String> keys;
}
