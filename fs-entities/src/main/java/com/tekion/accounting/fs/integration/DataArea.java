package com.tekion.accounting.fs.integration;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class DataArea {
    @XmlElement(name = "oa:Process")
    OAProcess oaProcess = new OAProcess();
    @XmlElement(name = "FinancialStatement")
    List<FinancialStatement> financialStatements;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    static class OAProcess{
        @XmlAttribute
        String confirm="Always";
        @XmlAttribute
        String acknowledge = "Always";
    }
}
