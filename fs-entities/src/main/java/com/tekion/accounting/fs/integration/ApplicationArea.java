package com.tekion.accounting.fs.integration;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationArea {
    @XmlElement(name = "Sender")
    Sender sender;
    @XmlElement(name = "CreationDateTime",defaultValue = "")
    String creationDateTime = "";
    @XmlElement(name = "Signature")
    Signature signature;
    @XmlElement(name = "BODId")
    String BODId;
    @XmlElement(name = "Destination")
    Destination destination;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Signature{
        @XmlAttribute
        String qualifyingAgency= "String";
    }
}
