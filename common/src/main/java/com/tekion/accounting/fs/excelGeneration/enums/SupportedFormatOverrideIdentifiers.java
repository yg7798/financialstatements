package com.tekion.accounting.fs.excelGeneration.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportedFormatOverrideIdentifiers {
    JOURNAL_NUMBER("Journal"),
    REFERENCE("Reference"),
    GL_ACCOUNT_NUMBER("GL Account"),
    VENDOR_NUMBER("Vendor Number"),
    CUSTOMER_NUMBER("Customer Number"),
    EMPLOYEE_NUMBER("Employee Number"),
    CONTROL_NUMBER("Control Number");

    private final String getDisplayName;
}
