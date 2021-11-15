package com.tekion.accounting.fs.dto.cellGrouop;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.common.utils.OemFSUtils;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FSCellGroupCodeCreateDto {

    private OEM oemId;
    @NotNull private Integer year;
    @NotNull private Integer version;
    @NotBlank private String country;

    @NotBlank private String groupDisplayName;
    private String cdkPcl;
    private String dbPcl;
    private String rrPcl;
    private String dominionPcl;
    private String quorumPcl;
    private String autosoftPcl;
    private String automatePcl;
    private String pbsPcl;
    @NotBlank private String oemAccountNumber;


    public AccountingOemFsCellGroup toOemFSCellGroup(){
        AccountingOemFsCellGroup g = new AccountingOemFsCellGroup();
        g.setOemId(oemId.name());
        g.setYear(year);
        g.setVersion(version);
        g.setCountry(country);
        g.setGroupDisplayName(groupDisplayName);
        g.setGroupCode(OemFSUtils.createGroupCode(groupDisplayName));
        g.setCdkPcl(cdkPcl);
        g.setDbPcl(dbPcl);
        g.setRrPcl(rrPcl);
        g.setDominionPcl(dominionPcl);
        g.setQuorumPcl(quorumPcl);
        g.setAutosoftPcl(autosoftPcl);
        g.setPbsPcl(pbsPcl);
        g.setOemAccountNumber(oemAccountNumber);
        g.setAutomatePcl(automatePcl);
        g.setCreatedTime(System.currentTimeMillis());
        g.setModifiedTime(System.currentTimeMillis());
        return g;

    }

}