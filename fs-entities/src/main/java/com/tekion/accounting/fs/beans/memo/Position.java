package com.tekion.accounting.fs.beans.memo;

import com.tekion.multilingual.commons.service.TekLanguage;
import com.tekion.multilingual.dto.TekMultiLingualBean;
import lombok.Data;

@Data
public class Position implements TekLanguage {
    public static String NAME = "name";

    private String key;
    private String name;
    int order;
    private String groupKey = "DEFAULT";
    private String sectionKey;
    private TekMultiLingualBean languages;

    @Override
    public TekMultiLingualBean getLanguages() {
        return languages;
    }
}
