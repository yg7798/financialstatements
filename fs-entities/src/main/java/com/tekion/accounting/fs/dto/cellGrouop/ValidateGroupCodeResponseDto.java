package com.tekion.accounting.fs.dto.cellGrouop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateGroupCodeResponseDto {

    List<String> groupCodesToRemove;
    List<String> groupCodesToAdd;

}
