package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.accounting.fs.service.fsMapping.FsMappingService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.tekion.accounting.fs.util.ApiHelperUtils.getDefaultParallelism;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/migration")
public class FSMigrationApi {

    private final FsEntryService fsEntryService;
    private final GlobalService globalService;
    private final FsMappingService fsMappingService;

    @PostMapping("/fsEntries/fromYear/{fromYear}/to/{toYear}")
    public ResponseEntity migrateFsEntriesFromYear(@PathVariable("fromYear") Integer fromYear, @PathVariable("toYear") Integer toYear){
        fsEntryService.migrateFsEntriesFromYearToYear(fromYear, toYear);
        return TResponseEntityBuilder.okResponseEntity("success");
    }

    @PostMapping("/fsEntries/fromYear/{fromYear}/to/{toYear}/all")
    public ResponseEntity migrateFsEntriesFromYearToYearForAllDealers(@PathVariable @NotNull Integer fromYear,
                                                                      @PathVariable @NotNull Integer toYear,
                                                                      @RequestParam(required = false) Integer parallelism){

        globalService.executeTaskForAllDealers(() -> fsEntryService.migrateFsEntriesFromYearToYear(fromYear, toYear),
                getDefaultParallelism(parallelism));
        return TResponseEntityBuilder.okResponseEntity("Success");
    }

    @PostMapping("/fsMappings/fromFsId/{fromFsId}/to/{toFsId}")
    public ResponseEntity migrateFsMappings(@PathVariable("fromFsId") String fromFsId, @PathVariable("toFsId") String toFsId){
        return TResponseEntityBuilder.okResponseEntity(fsMappingService.copyFsMappings(fromFsId, toFsId));
    }

    @PostMapping("/fsMappings/fromYear/{fromYear}/to/{toYear}")
    public ResponseEntity migrateFsMappingsFromYearToYear(@PathVariable @NotNull Integer fromYear,
                                                          @PathVariable @NotNull Integer toYear,
                                                          @RequestParam(required = false) Integer parallelism,
                                                          @RequestBody List<String> oemIds){
        fsMappingService.migrateFsMappingsFromYearToYear(fromYear, toYear, oemIds);
        return TResponseEntityBuilder.okResponseEntity("Success");
    }

    @PostMapping("/fsMappings/fromYear/{fromYear}/to/{toYear}/all")
    public ResponseEntity migrateFsMappingsFromYearToYearForAllDealers(@PathVariable @NotNull Integer fromYear,
                                                                      @PathVariable @NotNull Integer toYear,
                                                                      @RequestParam(required = false) Integer parallelism,
                                                                       @RequestBody List<String> oemIds){
        globalService.executeTaskForAllDealers(() -> fsMappingService.migrateFsMappingsFromYearToYear(fromYear, toYear, oemIds),
                getDefaultParallelism(parallelism));
        return TResponseEntityBuilder.okResponseEntity("Success");
    }

    @PostMapping("/fsEntry/migrateParentRef/year/{year}")
    public ResponseEntity addParentRefToFsEntry(@PathVariable @NotNull Integer year){
        fsEntryService.migrateParentRef(year);
        return TResponseEntityBuilder.okResponseEntity("Success");
    }

    @PostMapping("/fsEntry/migrateParentRef/year/{year}/all")
    public ResponseEntity addParentRefToAllFsEntries(@PathVariable @NotNull Integer year, @RequestParam(required = false) Integer parallelism){
        globalService.executeTaskForAllDealers(() -> fsEntryService.migrateParentRef(year), getDefaultParallelism(parallelism));
        return TResponseEntityBuilder.okResponseEntity("Success");
    }
}
