package com.tekion.accounting.fs.beans.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.core.beans.TBaseMongoBean;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndexes({
		@CompoundIndex(
				name = "idx_country_1_oemId_1_year_1",
				def = "{'country':1 ,'oemId':1, 'year':1}"
		)
})
public class OemTemplate extends TBaseMongoBean {

	public static final String OEM_ID = "oemId";
	public static final String ACTIVE = "active";
	public static final String YEAR = "year";
	public static final String MODIFIED_BY_USER_ID = "modifiedByUserId";

	private String oemId;
	private Integer year;
	private String country;
	private Object template;
	private boolean active;
	private Integer version;

	private String createdByUserId;
	private String modifiedByUserId;
}

