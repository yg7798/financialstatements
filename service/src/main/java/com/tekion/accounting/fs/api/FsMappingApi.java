package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.dto.mappings.OemIdsAndGroupCodeListRequestDto;
import com.tekion.accounting.fs.service.fsMapping.FsMappingService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static com.tekion.accounting.fs.util.ApiHelperUtils.getDefaultParallelism;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/fsMapping")
public class FsMappingApi {

    private final FsMappingService fsMappingService;
    private final GlobalService globalService;


    @DeleteMapping("/duplicateMapping")
    public ResponseEntity deleteGlAccountFsCellCodeDuplicateMapping(@NotNull @RequestBody List<String> fsIds) {
        log.info("api call received for deleting oem-Fs duplicate mapping for fsIds : {}", fsIds);
        return TResponseEntityBuilder.okResponseEntity(fsMappingService.deleteDuplicateMappings(fsIds));
    }

    @DeleteMapping("/invalidMapping/{fsId}")
    public ResponseEntity deleteOemFsMappingForNonExistingGlAccountOrFsCellCode(@PathVariable String fsId) {
        return TResponseEntityBuilder.okResponseEntity(fsMappingService.deleteInvalidMappings(fsId));
    }

    @PostMapping("/byGlAccounts/fsId/{fsId}")
    public ResponseEntity getMappingsByGLAccounts(@PathVariable String fsId, @NotNull @RequestBody List<String> glAccountIds) {
        return TResponseEntityBuilder.okResponseEntity(fsMappingService.getMappingsByGLAccounts(fsId, glAccountIds));
    }

    @PostMapping("/byOemIdAndGroupCode")
    public ResponseEntity getFsMappingsFromOemIdAndGroupCodes(@NotNull @RequestBody OemIdsAndGroupCodeListRequestDto requestDto) {
        return TResponseEntityBuilder.okResponseEntity(fsMappingService.getFsMappingsByOemIdAndGroupCodes(requestDto.getYear()
                ,requestDto.getGroupCodes(), requestDto.getOemIds(), false));
    }

    @DeleteMapping("/byGroupDisplayNames/oem/{oemId}/year/{year}/country/{country}")
    public ResponseEntity deleteMappingsByGroupCodes(@RequestBody List<String> groupDisplayNames,
                                                     @PathVariable Integer year,
                                                     @PathVariable String oemId,
                                                     @PathVariable String country) {
        fsMappingService.deleteMappingsByGroupCodes(groupDisplayNames, oemId, year, country);
        return TResponseEntityBuilder.okResponseEntity("success");
    }

    @DeleteMapping("/byGroupDisplayNames/oem/{oemId}/year/{year}/country/{country}/all")
    public ResponseEntity deleteMappingsByGroupCodes(@RequestBody List<String> groupDisplayNames,
                                                      @PathVariable Integer year,
                                                      @PathVariable String oemId,
                                                      @PathVariable String country,
                                                      @RequestParam(required = false) Integer parallelism) {

        globalService.executeTaskForAllDealers(() ->
                fsMappingService.deleteMappingsByGroupCodes(groupDisplayNames, oemId, year, country),
                getDefaultParallelism(parallelism));
        return TResponseEntityBuilder.okResponseEntity("success");
    }

    @PutMapping("/replaceGroupCodes/oem/{oemId}/year/{year}/country/{country}")
    public ResponseEntity replaceGroupCodesInMappings(@RequestBody Map<String, String> groupDisplayNames,
                                                     @PathVariable Integer year,
                                                     @PathVariable String oemId,
                                                      @PathVariable String country) {
        fsMappingService.replaceGroupCodesInMappings(groupDisplayNames, oemId, year, country);
        return TResponseEntityBuilder.okResponseEntity("success");
    }

    @PutMapping("/replaceGroupCodes/oem/{oemId}/year/{year}/country/{country}/all")
    public ResponseEntity replaceGroupCodesInMappings(@RequestBody Map<String, String> groupDisplayNames,
                                                      @PathVariable Integer year,
                                                      @PathVariable String oemId,
                                                      @PathVariable String country,
                                                      @RequestParam(required = false) Integer parallelism) {
        globalService.executeTaskForAllDealers(() -> fsMappingService.replaceGroupCodesInMappings(groupDisplayNames, oemId, year, country),
                getDefaultParallelism(parallelism));;
        return TResponseEntityBuilder.okResponseEntity("success");
    }
}