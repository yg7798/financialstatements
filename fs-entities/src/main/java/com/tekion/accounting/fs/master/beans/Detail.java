package com.tekion.accounting.fs.master.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Detail {
    @XmlElement(name = "AccountId")
    String accountId;
    @XmlElement(name = "AccountValue")
    String accountValue;
    @XmlTransient
    String oemCodeSign;
    @XmlTransient
    String description;
    @XmlTransient
    String unit1;
    @XmlTransient
    String unit2;
    @XmlTransient
    String balance1;
    @XmlTransient
    String balance2;
}
