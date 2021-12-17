package com.tekion.accounting.fs.client.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class OemIdsAndGroupCodeListDto {
    @NotNull
    Integer year;
    List<String> oemIds;
    List<String> groupCodes;
}
