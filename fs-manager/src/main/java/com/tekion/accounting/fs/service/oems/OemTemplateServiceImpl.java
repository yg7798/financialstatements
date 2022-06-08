package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.repos.OemTemplateRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OemTemplateServiceImpl implements OemTemplateService {
    private final OemTemplateRepo oemTemplateRepo;

    @Override
    public void migrateLocale(String country, String locale) {
        oemTemplateRepo.migrateLocale(country, locale);
    }
}
