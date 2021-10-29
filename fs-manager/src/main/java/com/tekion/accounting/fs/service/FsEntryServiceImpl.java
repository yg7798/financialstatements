package com.tekion.accounting.fs.service;

import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.as.client.AccountingClient;
import com.tekion.core.utils.UserContextProvider;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class FsEntryServiceImpl implements FsEntryService {

  public final FSEntryRepo fsEntryRepo;
  private final AccountingClient accountingClient;

  @Override
  public FSEntry getFSEntryById(String id) {
    return fsEntryRepo.findByIdAndDealerId(id, UserContextProvider.getCurrentDealerId());
  }
}
