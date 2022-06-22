package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.common.validation.OemFSMetadataMappingDtoValidatorGroup;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.fsMetaData.OemFSMetadataCellMappingCreateDto;
import com.tekion.accounting.fs.service.fsMetaData.OemFSMetadataMappingService;
import com.tekion.accounting.fs.service.fsMetaData.OemFsMetadataCellMappingInfo;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oemFsMetadata")
public class OemFsMetadataCellMappingApi {

    private final OemFSMetadataMappingService metadataMappingService;
    private final TValidator validator;

    @GetMapping("/oem/{oemId}/{year}/v/{version}")
    public ResponseEntity getOemFsMetaDataInfo(@PathVariable OEM oemId, @PathVariable @NotNull Integer year, @PathVariable @NotBlank String version) {
        return TResponseEntityBuilder.okResponseEntity(metadataMappingService.getOemFsMetadataCellMappings(oemId.name(), year, version));
    }

    @PostMapping("/")
    public ResponseEntity saveOemFsMetadataInfo(@RequestBody OemFSMetadataCellMappingCreateDto reqDto) {
        validator.validate(reqDto, OemFSMetadataMappingDtoValidatorGroup.class);
        metadataMappingService.saveOemFsMetadataCellMappings(reqDto);
        return TResponseEntityBuilder.okResponseEntity("done");
    }

    @PutMapping("/")
    public ResponseEntity updateOemFsMetadataInfo(@RequestBody OemFsMetadataCellMappingInfo reqDto) {
        validator.validate(reqDto, OemFSMetadataMappingDtoValidatorGroup.class);
        metadataMappingService.updateOemFsMetadataCellMappings(reqDto);
        return TResponseEntityBuilder.okResponseEntity("done");
    }

    @DeleteMapping("/country/{country}/oem/{oemId}/{year}/v/{version}")
    public ResponseEntity deleteOemFsMetadataInfo(@PathVariable OEM oemId, @PathVariable @NotNull Integer year, @PathVariable @NotBlank String version,
                                                  @PathVariable @NotBlank String country) {
        metadataMappingService.deleteOemFsMetadataCellMappings(oemId.name(), year, version, country);
        return TResponseEntityBuilder.okResponseEntity("done");
    }

}
