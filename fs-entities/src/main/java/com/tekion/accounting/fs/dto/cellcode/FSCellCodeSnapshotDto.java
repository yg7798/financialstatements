package com.tekion.accounting.fs.dto.cellcode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FSCellCodeSnapshotDto {
    @NotBlank String oemId;
    Integer year;
    List<Integer> snapshotMonths;
}
