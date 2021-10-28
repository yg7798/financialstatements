package com.tekion.accounting.fs.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FsMappingInfosResponseDto {

    private List<FsMappingInfo> fsMappingInfoList;

}
