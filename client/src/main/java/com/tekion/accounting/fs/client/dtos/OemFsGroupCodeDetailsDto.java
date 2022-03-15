package com.tekion.accounting.fs.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OemFsGroupCodeDetailsDto {
    private String oemId;
    private List<String> groupCodes;

}
