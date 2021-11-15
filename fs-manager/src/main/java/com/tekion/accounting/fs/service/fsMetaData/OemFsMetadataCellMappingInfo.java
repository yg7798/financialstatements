package com.tekion.accounting.fs.service.fsMetaData;

import com.tekion.accounting.fs.beans.common.CellAddressMapping;
import com.tekion.accounting.fs.common.validation.OemFSMetadataMappingDtoValidatorGroup;
import com.tekion.accounting.fs.beans.common.OemFSMetadataCellsInfo;
import com.tekion.accounting.fs.enums.OEM;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OemFsMetadataCellMappingInfo {
    @NotNull (groups = OemFSMetadataMappingDtoValidatorGroup.class)
    private OEM oem;
    @NotBlank (groups = OemFSMetadataMappingDtoValidatorGroup.class)
    private String version;
    @NotNull (groups = OemFSMetadataMappingDtoValidatorGroup.class)
    private Integer year;
    @NotEmpty (groups = OemFSMetadataMappingDtoValidatorGroup.class)
    private String country;
    @NotEmpty(groups = OemFSMetadataMappingDtoValidatorGroup.class)
    @Valid
    private List<CellAddressMapping> mappings = new ArrayList<>();

    public OemFSMetadataCellsInfo convertToDealerMappingInfo() {
        OemFSMetadataCellsInfo oemFSMetadataCellsInfo = new OemFSMetadataCellsInfo();
        oemFSMetadataCellsInfo.setOemId(this.oem.name());
        oemFSMetadataCellsInfo.setVersion(this.version);
        oemFSMetadataCellsInfo.setYear(this.year);
        oemFSMetadataCellsInfo.setCountry(this.country);
        oemFSMetadataCellsInfo.setCellMapping(this.mappings);
        return oemFSMetadataCellsInfo;
    }
}
