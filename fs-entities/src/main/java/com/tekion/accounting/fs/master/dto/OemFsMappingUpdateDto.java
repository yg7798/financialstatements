package com.tekion.accounting.fs.master.dto;

import com.tekion.accounting.fs.master.beans.OemFsMappingDetail;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OemFsMappingUpdateDto {

    @NotBlank
    private String fsId;
    private String siteId;
    @Valid @NotNull private List<OemFsMappingDetail> mappingsToSave;
    @Valid @NotNull private List<OemFsMappingDetail> mappingsToDelete;

}
