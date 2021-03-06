package com.tekion.accounting.fs.service.fsMetaData.models;

import com.tekion.accounting.fs.enums.OEM;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GLAccountOEMFSMappingDto {
  @NotNull
  private String year;

  @NotNull
  private List<OemAccountNumberDTO> mappings;


  @Data
  private class OemAccountNumberDTO{
    String mappingId;
    OEM oem;
    String oemAccountNumber;
  }
}

