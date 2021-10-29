package com.tekion.accounting.fs.dto.cellGrouop;

import com.google.common.collect.Lists;
import com.tekion.accounting.fs.beans.AccountingOemFsCellGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FSCellGroupCodesCreateDto {

    @NotEmpty private List<FSCellGroupCodeCreateDto> groups = Lists.newArrayList();


    public List<AccountingOemFsCellGroup> toOemFSCellGroupList(){
        return groups.stream().map(FSCellGroupCodeCreateDto::toOemFSCellGroup).collect(Collectors.toList());
    }

}