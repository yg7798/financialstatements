package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.service.fsMapping.FsMappingService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/fsMapping")
public class FsMappingApi {

    private final FsMappingService fsMappingService;

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

}