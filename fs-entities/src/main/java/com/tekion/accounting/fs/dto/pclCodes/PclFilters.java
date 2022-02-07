package com.tekion.accounting.fs.dto.pclCodes;

import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class PclFilters {
        Set<String> groupCode;
        Set<String> automatePcl;
        Set<String> autosoftPcl;
        Set<String> cdkPcl;
        Set<String> dbPcl;
        Set<String> dealerTrackPcl;
        Set<String> dominionPcl;
        Set<String> groupDisplayName;
        Set<String> pbsPcl;
        Set<String> quorumPcl;
        Set<String> rrPcl;
}
