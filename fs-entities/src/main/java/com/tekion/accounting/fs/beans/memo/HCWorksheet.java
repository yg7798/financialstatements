package com.tekion.accounting.fs.beans.memo;

import com.tekion.core.beans.TBaseMongoBean;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
@CompoundIndexes({@CompoundIndex(def = "{'oemId': 1,'year': 1, 'department': 1, 'position': 1}")})
public class HCWorksheet extends TBaseMongoBean {
    public static final String VALUES = "values";
    private String oemId;
    private int year;
    private int version;
    private String fsId;
    private String department;
    private String position;
    private List<HCValue> values;
    private String createdByUserId;
    private String modifiedByUserId;
    private String dealerId;
    private String tenantId;
    private String siteId;
}
