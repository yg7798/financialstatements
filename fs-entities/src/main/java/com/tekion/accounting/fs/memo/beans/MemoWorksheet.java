package com.tekion.accounting.fs.memo.beans;

import com.tekion.core.beans.TBaseMongoBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@CompoundIndex(def = "{ 'oemId':1,'year':1, 'version':1}")
public class MemoWorksheet extends TBaseMongoBean {
    public static final String VALUES = "values";
    public static final String FIELD_TYPE = "fieldType";
    public static final String ACTIVE = "active";

    private String oemId;
    private String key;
    private int year;
    private int version;
    private String fsId;
    private String fieldType;

    private String dealerId;
    private String siteId;
    private String createdByUserId;
    private String modifiedByUserId;

    private List<MemoValue> values;
    private Boolean active = true;

    // private String name;
    // private Set<OemCellDurationType> durationTypes;
}
