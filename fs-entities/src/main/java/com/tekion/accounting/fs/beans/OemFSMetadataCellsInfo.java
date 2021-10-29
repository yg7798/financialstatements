package com.tekion.accounting.fs.beans;

import com.tekion.accounting.fs.dto.CellAddressMapping;
import com.tekion.core.beans.TBaseMongoBean;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@CompoundIndexes({
		@CompoundIndex(
				name = "idx_country_oemId_year",
				def = "{'country':1, 'oemId':1, 'year':1 }"
		)
})

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OemFSMetadataCellsInfo extends TBaseMongoBean {

	public static final String OEM_ID = "oemId";
	public static final String YEAR = "year";
	public static final String VERSION = "version";

	private String oemId;
	private Integer year;
	private String version;
	private String country;

	private List<CellAddressMapping> cellMapping = new ArrayList<>();
}
