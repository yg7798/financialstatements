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
public class OemIdsAndGroupCodeListRequestDto {
    Integer year;
    List<String> oemIds;
    List<String> groupCodes;
}
