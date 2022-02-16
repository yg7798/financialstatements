package com.tekion.accounting.fs.beans.mappings;

import com.tekion.as.models.beans.MediaResponse;
import com.tekion.core.beans.TBaseMongoBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
public class OEMFinancialMappingMedia extends TBaseMongoBean {
	List<MediaResponse> medias;
	private String year;
	private String oemId;
	private String fsId;
	private String dealerId;
	private String tenantId;
}
