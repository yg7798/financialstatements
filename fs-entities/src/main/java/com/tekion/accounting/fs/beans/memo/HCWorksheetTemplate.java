package com.tekion.accounting.fs.beans.memo;

import com.tekion.core.beans.TBaseMongoBean;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Head count worksheet template
 */
@Document
@Data
@CompoundIndexes({
        @CompoundIndex(
                name = "idx_country_oemId_year",
                def = "{'country': 1, 'oemId': 1, 'year': 1}"
        )
})
public class HCWorksheetTemplate extends TBaseMongoBean {
    @NotEmpty
    private String oemId;
    private int year;
    private int version;
    @NotBlank
    private String country;
    private int precision;
    private List<Position> positions;
    private List<HCDepartment> departments;

    //<groupId, <groupMetadata, List<>>>
    private Map<String, Object> additionalInfo = new HashMap<>();
}
