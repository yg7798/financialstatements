package com.tekion.accounting.fs.dto.mappings;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OEMMappingDto {
  private String id;
  private String oemAccountNumber;
  @NotNull
  private String glAccountId;
  private String glAccountDealerId;

}
