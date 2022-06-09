package com.tekion.accounting.fs.beans.memo;

import com.tekion.accounting.fs.enums.OemCellDurationType;
import com.tekion.core.beans.TBaseMongoBean;
import com.tekion.multilingual.commons.service.TekLanguage;
import com.tekion.multilingual.dto.TekMultiLingualBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.Set;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndexes({
        @CompoundIndex(
                name = "idx_country_oemId_year_key",
                def = "{'country':1, 'oemId':1, 'year':1, 'key':1 }", unique = true
        )
})
public class MemoWorksheetTemplate extends TBaseMongoBean implements TekLanguage {
    public static String NAME = "name";
    public static String LANGUAGES = "languages";

    private String oemId;
    private String key;
    private String name;
    private Set<OemCellDurationType> durationTypes;
    private int year;
    private int version;
    private String country;
    private String lineNumber;
    private String pageNumber;
    private Map<String,String> additionalInfo;
    private String createdByUserId;
    private String modifiedByUserId;
    private FieldType fieldType;
    private TekMultiLingualBean languages;

    @Override
    public TekMultiLingualBean getLanguages() {
        return languages;
    }
}
