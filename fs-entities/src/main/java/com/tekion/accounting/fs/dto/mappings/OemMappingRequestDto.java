package com.tekion.accounting.fs.dto.mappings;

import com.tekion.as.models.beans.MediaResponse;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OemMappingRequestDto {
  @NotBlank
  private String fsId;
  @NotNull
  private String dealerId;
  @NotNull
  private List<OEMMappingDto> mappings;
  private List<MediaResponse> medias;
}
