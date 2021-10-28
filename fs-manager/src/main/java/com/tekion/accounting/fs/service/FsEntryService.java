package com.tekion.accounting.fs.service;

import com.tekion.accounting.fs.master.beans.FSEntry;

public interface FsEntryService {
  FSEntry getFSEntryById(String id);
}
