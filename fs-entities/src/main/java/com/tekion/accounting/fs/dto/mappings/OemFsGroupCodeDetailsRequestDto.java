package com.tekion.accounting.fs.dto.mappings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OemFsGroupCodeDetailsRequestDto {
    private String oemId;
    private List<String> groupCodes;
    private List<Integer> years;

}
