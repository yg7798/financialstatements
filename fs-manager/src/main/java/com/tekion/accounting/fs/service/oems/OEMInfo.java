package com.tekion.accounting.fs.service.oems;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OEMInfo {
    String oem;
    String brand;
}
