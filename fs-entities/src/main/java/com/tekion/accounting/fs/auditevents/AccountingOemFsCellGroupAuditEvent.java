package com.tekion.accounting.fs.auditevents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountingOemFsCellGroupAuditEvent extends AuditEvent{
    private String cdkPcl;
    private String dbPcl;
    private String rrPcl;
    private String dominionPcl;
    private String quorumPcl;
    private String autosoftPcl;
    private String automatePcl;
    private String pbsPcl;
    private String oemAccountNumber;
    private String dealerTrackPcl;
    private String disPcl;
}
