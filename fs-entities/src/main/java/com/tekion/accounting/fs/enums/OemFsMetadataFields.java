package com.tekion.accounting.fs.enums;

import lombok.Getter;

@Getter
public enum OemFsMetadataFields {

    DEALER_BAC_CODE("dealerBacCode"),
    DEALER_NAME("dealerName"),
    FROM_TIME( "fromTime"),
    TO_TIME("toTime"),
    FROM_DATE("fromDate"),
    TO_DATE ("toDate"),
    FROM_MONTH("fromMonth"),
    TO_MONTH( "toMonth"),
    FROM_YEAR( "fromYear"),
    TO_YEAR("toYear"),
    ADDRESS_LINE1( "addressLine1"),
    ADDRESS_LINE2( "addressLine2"),
    ADDRESS_LINE3("addressLine3"),
    ADDRESS_LINE4( "addressLine4"),
    ADDRESS_LINE5("addressLine5");

    private final String displayName;

    OemFsMetadataFields(String displayName) {
        this.displayName = displayName;
    }
}
