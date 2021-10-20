package com.tekion.accounting.fs.repos;

import com.tekion.accounting.fs.beans.FSEntry;

import java.util.List;

public interface FSEntryRepo {
  List<FSEntry> findByKey(String key, Object value, String dealerId, String siteId);

  FSEntry save(FSEntry accountingFSEntry);

  FSEntry findByIdAndDealerId(String id, String dealerId);
}
