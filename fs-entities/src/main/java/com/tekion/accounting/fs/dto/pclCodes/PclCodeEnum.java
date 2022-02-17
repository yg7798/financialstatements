package com.tekion.accounting.fs.dto.pclCodes;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public enum PclCodeEnum {
    AUTOMATE_PCL {
        public String getPclCode(AccountingOemFsCellGroup cellGroup) {
            return cellGroup.getAutomatePcl();
        }
        public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode) {
            cellGroup.setAutomatePcl(pclCode);
        }
    },
    AUTOSOFT_PCL{
        public String getPclCode(AccountingOemFsCellGroup cellGroup) {
            return cellGroup.getAutosoftPcl();
        }
        public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode) {
            cellGroup.setAutosoftPcl(pclCode);
        }
    },
    CDK_PCL{
        public String getPclCode(AccountingOemFsCellGroup cellGroup) {
            return cellGroup.getCdkPcl();
        }
        public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode) {
            cellGroup.setCdkPcl(pclCode);
        }
    },
    DB_PCL{
        public String getPclCode(AccountingOemFsCellGroup cellGroup) {
            return cellGroup.getDbPcl();
        }
        public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode) {
            cellGroup.setDbPcl(pclCode);
        }
    },
    DEALER_TRACK_PCL{
        public String getPclCode(AccountingOemFsCellGroup cellGroup) {
            return cellGroup.getDealerTrackPcl();
        }
        public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode) {
            cellGroup.setDealerTrackPcl(pclCode);
        }
    },
    DOMINION_PCL{
        public String getPclCode(AccountingOemFsCellGroup cellGroup) {
            return cellGroup.getDominionPcl();
        }
        public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode) {
            cellGroup.setDominionPcl(pclCode);
        }
    },
    PBS_PCL{
        public String getPclCode(AccountingOemFsCellGroup cellGroup) {
            return cellGroup.getPbsPcl();
        }
        public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode) {
            cellGroup.setPbsPcl(pclCode);
        }
    },
    QUORUM_PCL{
        public String getPclCode(AccountingOemFsCellGroup cellGroup) {
            return cellGroup.getQuorumPcl();
        }
        public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode) {
            cellGroup.setQuorumPcl(pclCode);
        }
    },
    RR_PCL{
        public String getPclCode(AccountingOemFsCellGroup cellGroup) {
            return cellGroup.getRrPcl();
        }
        public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode) {
            cellGroup.setRrPcl(pclCode);
        }
    };

    abstract public String getPclCode(AccountingOemFsCellGroup cellGroup);
    abstract public void setPclCode(AccountingOemFsCellGroup cellGroup, String pclCode);
}
