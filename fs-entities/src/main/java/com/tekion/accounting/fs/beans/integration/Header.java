package com.tekion.accounting.fs.beans.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Header {
    @XmlElement(name = "DocumentDateTime")
    String documentDateTime;
    @JsonProperty( value = "statementYearMonth")
    @XmlElement(name = "AccountingDate")
    String accountingDate;
    @JsonProperty( value = "accountingTermCode")
    @XmlElement(name = "AccountingTerm")
    String AccountingTerm;
    @XmlElement(name = "Count")
    String Count;
    String dealerCode;
}
