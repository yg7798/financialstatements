package com.tekion.accounting.fs.beans.mappings;


import com.tekion.core.beans.TBaseMongoBean;
import com.tekion.core.utils.UserContextProvider;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@CompoundIndexes({
		@CompoundIndex(
				name = "idx_year_month_groupcode",
				def = "{'year':1,'month':1,'fsCellGroupCode':1}"
		),
		@CompoundIndex(
				name = "idx_fsId_month_groupcode",
				def = "{'fsId':1,'month':1,'fsCellGroupCode':1}"
		)
})

@Data
@Document
public class OemFsMappingSnapshot extends TBaseMongoBean {
	private String oemId;
	private Integer year;
	private Integer version;
	private String fsId;
	// month values are in {1, 2, 3, ..,12}
	private Integer month;

	private String glAccountId;
	private String fsCellGroupCode;
	private String siteId;

	private String dealerId;
	private String tenantId;
	private String createdByUserId;

	public static OemFsMappingSnapshot fromOemMapping(OemFsMapping oemFsMapping,int month, String fsId){
		OemFsMappingSnapshot oemFsMappingSnapshot =  new OemFsMappingSnapshot();
		oemFsMappingSnapshot.setFsId(fsId);
		oemFsMappingSnapshot.setDealerId(oemFsMapping.getDealerId());
		oemFsMappingSnapshot.setTenantId(oemFsMapping.getTenantId());
		oemFsMappingSnapshot.setSiteId(oemFsMapping.getSiteId());
		oemFsMappingSnapshot.setGlAccountId(oemFsMapping.getGlAccountId());
		oemFsMappingSnapshot.setCreatedByUserId(UserContextProvider.getCurrentUserId());
		oemFsMappingSnapshot.setFsCellGroupCode(oemFsMapping.getFsCellGroupCode());
		oemFsMappingSnapshot.setMonth(month);
		oemFsMappingSnapshot.setOemId(oemFsMapping.getOemId());
		oemFsMappingSnapshot.setCreatedTime(System.currentTimeMillis());
		oemFsMappingSnapshot.setModifiedTime(System.currentTimeMillis());
		oemFsMappingSnapshot.setYear(oemFsMapping.getYear());
		oemFsMappingSnapshot.setVersion(oemFsMapping.getVersion());
		return oemFsMappingSnapshot;
	}

	public static OemFsMapping toOemMapping(OemFsMappingSnapshot snapshot){
		OemFsMapping mapping = new OemFsMapping();
		mapping.setFsCellGroupCode(snapshot.getFsCellGroupCode());
		mapping.setGlAccountId(snapshot.getGlAccountId());
		mapping.setYear(snapshot.getYear());
		mapping.setDealerId(snapshot.getDealerId());
		mapping.setTenantId(snapshot.getTenantId());
		mapping.setSiteId(snapshot.getSiteId());
		mapping.setOemId(snapshot.getOemId());
		return mapping;
	}
}
