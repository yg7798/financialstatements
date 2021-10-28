package com.tekion.accounting.fs.master.dto;

import com.tekion.accounting.fs.master.beans.OEMFinancialMapping;
import com.tekion.as.models.beans.MediaResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OEMMappingResponse {
  private List<OEMFinancialMapping> mappings;
  private List<MediaResponse> medias;
}
