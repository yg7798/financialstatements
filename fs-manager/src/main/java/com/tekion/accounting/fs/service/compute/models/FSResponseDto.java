package com.tekion.accounting.fs.service.compute.models;

import com.tekion.accounting.fs.dto.mappings.OemFsMappingDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FSResponseDto {

    private List<OemFsMappingDetails> oemFsMappingResponseDtoList;
    private int mappedAccounts;
    private int unmappedAccounts;

}
