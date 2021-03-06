package com.tekion.accounting.fs.repos;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;

import java.util.List;

public interface FSCellCodeRepo {

    List<AccountingOemFsCellCode> getFsCellCodesForOemYearAndCountry(String oemId, Integer year, Integer version, String country);

    AccountingOemFsCellCode save(AccountingOemFsCellCode fsCellCode);

    void insertBulk(List<AccountingOemFsCellCode>beans);

    AccountingOemFsCellCode findByCodeOemIdYearAndCountry(String fsCellCode, Integer year, String oemId, String country);

    List<AccountingOemFsCellCode> findByCodesAndDealerIdAndOemIdNonDeleted(List<String> fsCellCodes, Integer year, String oemId, String country);

    List<BulkWriteUpsert> updateBulk(List<AccountingOemFsCellCode> fsCellCodes);

    List<BulkWriteUpsert> updateBulkOemCode(List<AccountingOemFsCellCode> fsCellCodes);

    List<BulkWriteUpsert> delete(List<AccountingOemFsCellCode> fsCellCodes);

    void addCountryInOemFsCellCodes();

    void remove(String oem, Integer year, String country);
}
