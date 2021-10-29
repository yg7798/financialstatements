package com.tekion.accounting.fs.dto.memo;

import com.tekion.accounting.fs.beans.memo.HCValue;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class HCUpdateDto {
    @NotEmpty
    private String id;
    @NotNull
    private List<HCValue> values;
}
