package com.tekion.accounting.fs.integration;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class FinancialStatement {
    @XmlElement(name = "Header")
    Header header;
    @XmlElement(name = "Detail")
    List<Detail> details = new ArrayList<>();
}
