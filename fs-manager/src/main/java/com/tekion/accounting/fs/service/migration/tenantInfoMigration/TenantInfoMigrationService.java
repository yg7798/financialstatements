package com.tekion.accounting.fs.service.migration.tenantInfoMigration;

public interface TenantInfoMigrationService {
    Integer addTenantDealerInfoInMongoBeans(MongoCollectionEnum mongoCollectionEnum);
}
