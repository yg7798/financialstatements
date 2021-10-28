package com.tekion.accounting.fs.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FsGroupCodeDetail {

   private BigDecimal mtdBalance = BigDecimal.ZERO;
   private BigDecimal ytdBalance = BigDecimal.ZERO;
   private Long mtdCount;
   private Long ytdCount;
   private List<String> dependentGlAccounts;

}

