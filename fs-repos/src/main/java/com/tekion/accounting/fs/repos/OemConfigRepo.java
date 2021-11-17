package com.tekion.accounting.fs.repos;

import com.tekion.accounting.fs.beans.common.OemConfig;

public interface OemConfigRepo {

    OemConfig save(OemConfig check);

    OemConfig findByOemId(String oemId, String country);

    void addCountryInOemConfigs();
}
