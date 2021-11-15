package com.tekion.accounting.fs.beans.common;

import com.tekion.accounting.fs.common.validation.OemFSMetadataMappingDtoValidatorGroup;
import com.tekion.accounting.fs.integration.CellAddressInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CellAddressMapping {
    @NotNull (groups = OemFSMetadataMappingDtoValidatorGroup.class)
    @Valid
    private CellAddressInfo cellAddressInfo;
    @NotBlank (groups = OemFSMetadataMappingDtoValidatorGroup.class)
    private String type;
}