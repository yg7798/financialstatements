package com.tekion.accounting.fs.repos;

import com.tekion.accounting.fs.beans.OemConfig;

public interface OemConfigRepo {

    OemConfig save(OemConfig check);

    OemConfig findByOemId(String oemId, String country);

    void addCountryInOemConfigs();
}
