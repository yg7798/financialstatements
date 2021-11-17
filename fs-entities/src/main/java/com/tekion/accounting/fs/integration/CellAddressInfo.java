package com.tekion.accounting.fs.integration;

import com.tekion.accounting.fs.common.validation.OemFSMetadataMappingDtoValidatorGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CellAddressInfo {
	@NotBlank(groups = OemFSMetadataMappingDtoValidatorGroup.class)
	private String page;
	@NotBlank (groups = OemFSMetadataMappingDtoValidatorGroup.class)
	private String cellAddress;
}
