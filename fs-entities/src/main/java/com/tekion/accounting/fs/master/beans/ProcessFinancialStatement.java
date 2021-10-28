package com.tekion.accounting.fs.master.beans;

import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
@XmlRootElement(name = "ProcessFinancialStatement")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessFinancialStatement {
    @XmlAttribute
    String xmlns="http://www.openapplications.org/star";
    @XmlAttribute(name = "xmlns:oa")
    String xmlnsoa="http://www.openapplications.org/oagis";
    @XmlAttribute(name = "xmlns:xsi")
    String xmlnsxsi="http://www.w3.org/2001/XMLSchema-instance";
    @XmlAttribute(name = " xsi:schemaLocation")
    String xsischemaLocation="http://www.openapplications.org/star../BODs/ProcessFinancialStatement.xsd";
    @XmlAttribute
    String revision="1.0";
    @XmlAttribute
    String release="8.1";
    @XmlAttribute
    String environment="Production";
    @XmlAttribute
    String lang="en-US";

    @XmlElement(name = "ApplicationArea")
    ApplicationArea applicationArea;

    @XmlElement(name = "DataArea")
    DataArea dataArea;
}
