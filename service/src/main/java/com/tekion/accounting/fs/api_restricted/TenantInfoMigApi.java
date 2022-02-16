package com.tekion.accounting.fs.api_restricted;

import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.service.migration.tenantInfoMigration.MongoCollectionEnum;
import com.tekion.accounting.fs.service.migration.tenantInfoMigration.TenantInfoMigrationService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dealerTenantInfo")
@RequiredArgsConstructor
@Slf4j
public class TenantInfoMigApi {
    private final TenantInfoMigrationService tenantInfoMigrationService;
    private final GlobalService globalService;

    @PutMapping("/migrateTenantId/{mongoCollectionName}")
    public ResponseEntity addTenantIdInAllToAllScheduleDocuments(@PathVariable("mongoCollectionName") MongoCollectionEnum mongoCollectionEnum) {
        return TResponseEntityBuilder.okResponseEntity(tenantInfoMigrationService.addTenantDealerInfoInMongoBeans(mongoCollectionEnum));
    }

    @PutMapping("/migrateTenantId/{mongoCollectionName}/all")
    public ResponseEntity addTenantIdInAllDocuments(@PathVariable("mongoCollectionName") MongoCollectionEnum mongoCollectionEnum, @RequestParam(required = false) Integer parallelism) {
        if (parallelism == null) {
            parallelism = 1;
        }
        globalService.executeTaskForAllDealers(() -> tenantInfoMigrationService.addTenantDealerInfoInMongoBeans(mongoCollectionEnum), parallelism);
        return TResponseEntityBuilder.okResponseEntity("Success");
    }
}
