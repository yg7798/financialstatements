package com.tekion.accounting.fs.beans.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.core.beans.TBaseMongoBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Document
@CompoundIndexes({
		@CompoundIndex(def = "{'timestamp' : 1, 'code' : 1}"),
		@CompoundIndex(def = "{'fsId': 1, 'code': 1, 'deleted': 1 }"),
		@CompoundIndex(def = "{'code': 1, 'year':1 ,'month': 1, 'oemId': 1, 'deleted': 1 }"),
		@CompoundIndex(def = "{'fsId': 1, 'month':1, 'code': 1,  'deleted': 1 }")
})
public class OEMFsCellCodeSnapshot extends TBaseMongoBean {
	private int year;
	private int month;
	private String oemId;
	private String fsId;
	private int version;
	private String dealerId;
	private String code;
	private BigDecimal value;
	private Long timestamp;
	private String siteId;
	private String fsType;

	public OEMFsCellCodeSnapshot(String fsId, int year, int month, String dealerId, int version, String siteId, String fsType) {
		this.fsId = fsId;
		this.year = year;
		this.month = month;
		this.dealerId = dealerId;
		this.version = version;
		this.siteId = siteId;
		this.fsType = fsType;
	}
}
