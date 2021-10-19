package com.tekion.accounting.fs.service;

import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.core.utils.UserContextProvider;
import org.springframework.stereotype.Component;

@Component
public class FsEntryServiceImpl implements FsEntryService {

  public final FSEntryRepo fsEntryRepo;

  public FsEntryServiceImpl(FSEntryRepo fsEntryRepo) {
    this.fsEntryRepo = fsEntryRepo;
  }

  @Override
  public FSEntry getFSEntryById(String id) {
    return fsEntryRepo.findByIdAndDealerId(id, UserContextProvider.getCurrentDealerId());
  }
}
