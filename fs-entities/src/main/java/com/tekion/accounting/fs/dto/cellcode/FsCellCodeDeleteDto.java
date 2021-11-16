package com.tekion.accounting.fs.dto.cellcode;

import com.tekion.accounting.fs.enums.OEM;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FsCellCodeDeleteDto {
    List<String> cellCodes = new ArrayList<>();
    int year;
    @NotNull
    OEM oemId;
    @NotBlank
    String country;
}
