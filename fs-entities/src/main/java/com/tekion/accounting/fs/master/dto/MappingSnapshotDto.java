package com.tekion.accounting.fs.master.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class MappingSnapshotDto {
    @NotBlank String fsId;
    int month;
    Integer snapshotYear;
    List<Integer> snapshotMonths;
}
