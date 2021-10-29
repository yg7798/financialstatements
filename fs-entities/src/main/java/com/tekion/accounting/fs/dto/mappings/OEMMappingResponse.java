package com.tekion.accounting.fs.dto.mappings;

import com.tekion.accounting.fs.beans.mappings.OEMFinancialMapping;
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
