package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class FsApi {
  private final FsEntryService fsEntryService;
  private final TValidator validator;
}
