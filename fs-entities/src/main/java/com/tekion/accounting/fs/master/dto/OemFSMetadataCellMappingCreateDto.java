package com.tekion.accounting.fs.master.dto;

import com.tekion.accounting.fs.OemFSMetadataMappingDtoValidatorGroup;
import com.tekion.accounting.fs.master.beans.OemFSMetadataCellsInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.util.Lists;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OemFSMetadataCellMappingCreateDto {

    @NotEmpty(groups = OemFSMetadataMappingDtoValidatorGroup.class)
    @Valid
    private List<OemFsMetadataCellMappingInfo> oemFsMetadataCellMappingInfos;

    public List<OemFSMetadataCellsInfo> convertToDealerInfoMappingList() {
        List<OemFSMetadataCellsInfo> oemFSMetadataCellsInfoList = Lists.newArrayList();
        this.oemFsMetadataCellMappingInfos.forEach(oemFsMetadataCellMappingInfo -> {
            oemFSMetadataCellsInfoList.add(oemFsMetadataCellMappingInfo.convertToDealerMappingInfo());
        });
        return oemFSMetadataCellsInfoList;
    }
}
