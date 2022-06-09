package com.tekion.accounting.fs.service.multilingual;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;
import com.tekion.accounting.fs.beans.memo.Position;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetTemplateRepo;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.multilingual.commons.service.dbtype.DBTypeMultiLingualService;
import com.tekion.multilingual.dto.MultiLingualExportRequest;
import com.tekion.multilingual.dto.MultiLingualImportRequest;
import com.tekion.multilingual.dto.Record;
import com.tekion.multilingual.dto.TekMultiLingualBean;
import com.tekion.multilingual.dto.export.ExportResponse;
import com.tekion.multilingual.dto.importml.ImportResponse;
import com.tekion.tekionconstant.locale.TekLocale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class HCPositionService implements DBTypeMultiLingualService {
    public static final String POSITION_MULTILINGUAL_ASSET_NAME = "HCPosition";
    private final String DELIMITER = "&&";

    @Autowired
    HCWorksheetTemplateRepo hcWorksheetTemplateRepo;

    @Override
    public ExportResponse exportMultilingualRecords(MultiLingualExportRequest request) {
        log.info("exportMultilingualRecords request received {}", request);
        List<HCWorksheetTemplate> response = hcWorksheetTemplateRepo.findBySortByIdAndPageToken(request.getNextPageToken(), request.getBatchSize());
        ExportResponse exportResponse = transformToExportResponse(response);
        log.info("MultiLingualExportRecords request is {}, ExportResponse is {}", request, exportResponse);
        return exportResponse;
    }

    @Override
    public ImportResponse importMultilingualRecords(MultiLingualImportRequest request) {
        log.info("importMultilingualRecords request received {}", request);
        if(request.getAssetName().equalsIgnoreCase(getMultilingualAssetName())){
            Set<String> hcTemplateIds = new HashSet<>();
            Map<String, TekMultiLingualBean> keyToValueMap = Maps.newHashMap();
            request.getRecords().forEach(record -> {
                String id = record.getId().split(DELIMITER)[0];
                keyToValueMap.put(record.getId(), record.getTekMultiLingualBean());
                hcTemplateIds.add(id);
            });
            return updateLanguageInTemplates(hcTemplateIds, keyToValueMap);
        }
        return new ImportResponse();
    }

    @Override
    public String getMultilingualAssetName() {
        return POSITION_MULTILINGUAL_ASSET_NAME;
    }

    private ImportResponse updateLanguageInTemplates(Set<String> hcTemplateIds, Map<String,TekMultiLingualBean> keyToValueMap) {
        List<HCWorksheetTemplate> templates = hcWorksheetTemplateRepo.findByIds(hcTemplateIds);
        ImportResponse importResponse = new ImportResponse();
        importResponse.setAssetName(getMultilingualAssetName());
        List<Record> records = new ArrayList<>();
        templates.forEach(template -> {
            template.getPositions().forEach(position -> {
                Record record = new Record();
                String id = template.getId() + DELIMITER + position.getKey();
                position.setLanguages(keyToValueMap.get(id));
                record.setId(id);
                record.setTekMultiLingualBean(keyToValueMap.get(id));
                record.setTenantId(TConstants.ZERO_STRING);
                record.setDealerId(TConstants.ZERO_STRING);
                records.add(record);
            });
        });
        importResponse.setRecords(records);
        hcWorksheetTemplateRepo.upsertBulk(templates);
        return importResponse;
    }

    private ExportResponse transformToExportResponse(List<HCWorksheetTemplate> templates) {
        ExportResponse exportResponse = new ExportResponse();
        exportResponse.setAssetName(getMultilingualAssetName());
        if(!templates.isEmpty())
            exportResponse.setNextPageToken(templates.get(templates.size()-1).getId());

        List<Record> records = new ArrayList<>();
        templates.forEach(template -> {
            template.getPositions().forEach(position -> {
                Record record = new Record();
                record.setId(template.getId() + DELIMITER + position.getKey());
                record.setDealerId(TConstants.ZERO_STRING);
                record.setTenantId(TConstants.ZERO_STRING);
                TekMultiLingualBean languages = position.getLanguages();
                if (Objects.isNull(languages) || TCollectionUtils.isEmpty(languages.getLocale())) {
                    Map<TekLocale, Map<String, Object>> locale = new HashMap<>();

                    Map<String, Object> keyToValueMap = new HashMap<>();
                    keyToValueMap.put(Position.NAME, position.getName());
                    locale.put(TekLocale.en, keyToValueMap);
                    locale.put(TekLocale.en_US, keyToValueMap);

                    languages = new TekMultiLingualBean();
                    languages.setLocale(locale);
                    record.setTekMultiLingualBean(languages);
                    records.add(record);
                }
            });
        });
        exportResponse.setRecords(records);
        return exportResponse;
    }
}
