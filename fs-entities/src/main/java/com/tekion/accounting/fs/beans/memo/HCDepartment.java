package com.tekion.accounting.fs.beans.memo;

import com.tekion.multilingual.commons.service.TekLanguage;
import com.tekion.multilingual.dto.TekMultiLingualBean;
import lombok.Data;

import java.util.List;

@Data
public class HCDepartment implements TekLanguage {
    public static String NAME = "name";
    public static String LANGUAGES = "languages";

    private String key;
    private String name;
    private int order;
    private String subDepartment;
    private List<String> supportedPositions;
    private TekMultiLingualBean languages;

    @Override
    public TekMultiLingualBean getLanguages() {
        return languages;
    }
}
