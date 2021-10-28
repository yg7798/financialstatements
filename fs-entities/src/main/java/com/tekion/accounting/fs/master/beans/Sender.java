package com.tekion.accounting.fs.master.beans;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Sender {
    @XmlElement(name = "Component", defaultValue = "ELITE 2.0")
    String component = "ELITE 2.0";
    @XmlElement(name = "Task")
    String task = "StandardCodes";
    @XmlElement(name = "CreatorNameCode")
    String creatorNameCode = "";
    @XmlElement(name = "SenderNameCode")
    String senderNameCode = "AF";
    @XmlElement(name = "DealerNumber", defaultValue = "100001")
    String dealerNumber;
}
