package com.tekion.accounting.fs.master.dto;

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
