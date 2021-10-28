package com.tekion.accounting.fs.master.beans;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Destination {
    @XmlElement(name = "DestinationNameCode")
    String DestinationNameCode;
    @XmlElement(name = "DealerNumber", defaultValue = "100001")
    String dealerNumber;
}
